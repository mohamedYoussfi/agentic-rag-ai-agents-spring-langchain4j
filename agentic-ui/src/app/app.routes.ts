import { Routes } from '@angular/router';
import {TransactionsComponent} from "./transactions/transactions.component";
import {ChatUiComponent} from "./chat-ui/chat-ui.component";

export const routes: Routes = [
  {
    path : "transactions", component : TransactionsComponent,
  },
  {
    path : "chat-ui", component : ChatUiComponent,
  },
];
