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
import { LandingPageComponent } from './components/landing-page/landing-page.component';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { HeaderComponent } from './components/header/header.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AboutUsComponent } from './components/about-us/about-us.component';
import { ProfileComponent } from './components/profile/profile.component';


@NgModule({
  declarations: [
    AppComponent,
    AINoteDetailComponent,
    AuthComponent,
    LandingPageComponent,
    HeaderComponent,
    AboutUsComponent,
    ProfileComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    RouterModule.forRoot(routes),
    MatToolbarModule,
    MatButtonModule,
    BrowserAnimationsModule,
    MatCardModule
  ],
  providers: [AINoteService, AuthService, provideAnimationsAsync()],
  bootstrap: [AppComponent]
})
export class AppModule { }