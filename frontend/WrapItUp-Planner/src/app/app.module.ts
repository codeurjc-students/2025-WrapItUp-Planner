import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { AppComponent } from './app.component';
import { AINoteDetailComponent } from './components/notes-details.component';
import { AINoteService } from './services/ainote.service';
import { routes } from './app.routes';



@NgModule({
  declarations: [
    AppComponent,
    AINoteDetailComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    RouterModule.forRoot(routes)
  ],
  providers: [AINoteService],
  bootstrap: [AppComponent]
})
export class AppModule { }