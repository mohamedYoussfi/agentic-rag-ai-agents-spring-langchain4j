import {Component} from '@angular/core';
import {FormsModule} from "@angular/forms";
import {HttpClient, HttpDownloadProgressEvent, HttpEventType} from "@angular/common/http";
import {MarkdownComponent} from "ngx-markdown";

@Component({
  selector: 'app-chat-ui',
  standalone: true,
  imports: [
    FormsModule,
    MarkdownComponent
  ],
  templateUrl: './chat-ui.component.html',
  styleUrl: './chat-ui.component.css'
})
export class ChatUiComponent {
  question :any;
  response : any;

  constructor(private http: HttpClient) {
  }

  askAgent() {
    this.response="";
     this.http.get("http://localhost:8091/askAgent?question="+this.question,
       {responseType : 'text', observe : 'events', reportProgress :true})
       .subscribe({
         next : evt => {
           if(evt.type ===HttpEventType.DownloadProgress){
             this.response = (evt as HttpDownloadProgressEvent).partialText
           }
         },
         error : err => {
           console.log(err);
           console.log(err);
         },
         complete :() => {

         }
       })
  }
}
