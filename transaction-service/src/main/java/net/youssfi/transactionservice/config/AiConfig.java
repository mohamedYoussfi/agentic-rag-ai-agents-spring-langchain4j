package net.youssfi.transactionservice.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Configuration
public class AiConfig {
    @Bean
    ChatMemoryProvider chatMemoryProvider(Tokenizer tokenizer){
        return chatId-> MessageWindowChatMemory.withMaxMessages(10);
    }
    @Bean
    EmbeddingStore<TextSegment> embeddingStore(EmbeddingModel embeddingModel){
        //return new InMemoryEmbeddingStore<>();


        return PgVectorEmbeddingStore.builder()
                .host("localhost")
                .port(5432)
                .database("agenticRagDb")
                .user("admin")
                .password("1234")
                .table("data_vs_v3")
                .dimension(embeddingModel.dimension())
                .dropTableFirst(true)
                .build();


    }

    @Bean
    ApplicationRunner loadDocumentToVectorStore(
            ChatLanguageModel chatLanguageModel,
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore,
            Tokenizer tokenizer,
            @Value("classpath:/docs/rapport.txt") Resource textResource,
            @Value("classpath:/docs") Resource folderResource,
            @Value("classpath:/docs/cv.pdf") Resource pdfResource){
        return args -> {

            //List<Document> documents = FileSystemDocumentLoader.loadDocuments(folderResource.getFile().toPath());
            //EmbeddingStoreIngestor.ingest(documents, embeddingStore);
            var doc = FileSystemDocumentLoader.loadDocument(pdfResource.getFile().toPath());

            var ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(DocumentSplitters.recursive(1000,100, tokenizer))
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();
            DocumentParser parser = new ApachePdfBoxDocumentParser();
            Document document = parser.parse(pdfResource.getInputStream());
            //ingestor.ingest(document);
            loadDataIntoVectorStore(pdfResource, tokenizer, chatLanguageModel,ingestor);
        };


    }

    @Bean
    ContentRetriever contentRetriever(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore){

        return EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(2)
                .minScore(0.6)
                .build();
    }

    public void loadDataIntoVectorStore(Resource pdfResource,
                                        Tokenizer tokenizer,
                                        ChatLanguageModel chatLanguageModel,
                                        EmbeddingStoreIngestor embeddingStoreIngestor
    ) throws IOException, IOException {

        PDDocument document = PDDocument.load(pdfResource.getFile());
        PDPageTree pdPages = document.getPages();
        PDFTextStripper pdfTextStripper = new PDFTextStripper();
        int index = 0;
        int page = 0;
        DocumentSplitter documentSplitter = DocumentSplitters.recursive(1000, 100, tokenizer);
        //TextSplitter textSplitter = new TokenTextSplitter();
        List<Document> imagesDocuments = new ArrayList<>();
        for (PDPage pdPage : pdPages) {
            ++page;
            pdfTextStripper.setStartPage(page);
            pdfTextStripper.setEndPage(page);
            PDResources resources = pdPage.getResources();
            List<String> media = new ArrayList<>();
            String textContent = pdfTextStripper.getText(document);
            List<Document> documentList = new ArrayList<>();
            for (var c : resources.getXObjectNames()) {
                PDXObject pdxObject = resources.getXObject(c);
                if (pdxObject instanceof PDImageXObject image) {
                    ++index;
                    BufferedImage imageImage = image.getImage();
                    String imagePath = "images/page_" + page + "_im_" + index + ".png";
                    FileOutputStream fileOutputStream = new FileOutputStream(imagePath);
                    ImageIO.write(imageImage, "png", fileOutputStream);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ImageIO.write(imageImage, "png", byteArrayOutputStream);
                    byte[] data = byteArrayOutputStream.toByteArray();
                    String imageBase64 = Base64.getEncoder().encodeToString(data);
                    Image img = Image.builder()
                            .base64Data(imageBase64)
                            .mimeType("image/png")
                            .build();
                    ImageContent imageContent1 = ImageContent.from(img);
                    //Media media1 = new Media(MimeTypeUtils.IMAGE_PNG, new ByteArrayResource(data));
                    media.add(imagePath);

                    UserMessage userMessage = UserMessage.from(
                            TextContent.from("Give me a description of  the provided image"),
                            imageContent1
                    );

                    Response<AiMessage> response = chatLanguageModel.generate(userMessage);
                    String imageDescription = response.content().text();
                    System.out.println(imageDescription);
                    textContent = textContent + "\n" + "IMAGE : " + imagePath + "\n" + "Desciption of the image :\n" + imageDescription;
                    Metadata metadata = new Metadata();
                    metadata.put("Page", page);
                    metadata.put("media", imagePath);
                    Document doc = new Document(imageDescription, metadata);
                    imagesDocuments.add(doc);
                }
                Metadata metadata = new Metadata();
                metadata.add("Page", page);
                metadata.add("media", media);
                Document pageDoc = new Document(textContent, metadata);
                //List<Document> chunks = textSplitter.split(pageDoc);
                //List<Document> chunksWithMedia = chunks.stream().map(d -> new Document(d.getContent(), pageDoc.getMedia(), d.getMetadata())).toList();
                //vectorStore.add(imagesDocuments);
                embeddingStoreIngestor.ingest(pageDoc);
            }
        }
    }
}
