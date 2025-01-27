import {Component, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [],
  templateUrl: './transactions.component.html',
  styleUrl: './transactions.component.css'
})
export class TransactionsComponent implements OnInit{
  transactions : any;
  constructor(private http : HttpClient) {
  }
 ngOnInit() {
    this.http.get("http://localhost:8091/transactions")
      .subscribe({
        next : data => {
          this.transactions = data;
        },
        error : err => {
          console.log(err);
        }
      })
 }
}
