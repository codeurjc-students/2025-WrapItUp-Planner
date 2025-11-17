import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { AppComponent } from './app.component';
import { AINoteDetailComponent } from './components/notes-details.component';
import { AINoteService } from './services/ainote.service';
import { AuthComponent } from './components/auth/auth.component';
import { AuthService } from './services/auth.service';
import { routes } from './app.routes';



@NgModule({
  declarations: [
    AppComponent,
    AINoteDetailComponent,
    AuthComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    RouterModule.forRoot(routes)
  ],
  providers: [AINoteService, AuthService],
  bootstrap: [AppComponent]
})
export class AppModule { }