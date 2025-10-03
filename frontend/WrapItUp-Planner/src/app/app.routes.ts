import { RouterModule, Routes } from '@angular/router';
import { AINoteDetailComponent } from './components/notes-details.component';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';


export const routes: Routes = [
    { path: 'notes', component: AINoteDetailComponent },
    { path: '', redirectTo: '/notes', pathMatch: 'full' },
    { path: 'notes/:id', component: AINoteDetailComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule, FormsModule]
})
export class AppRoutingModule { }