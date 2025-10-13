import { RouterModule, Routes } from '@angular/router';
import { AINoteDetailComponent } from './components/notes-details.component';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';


export const routes: Routes = [
    { path: 'notes/:id', component: AINoteDetailComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: false })],
  exports: [RouterModule, FormsModule]
})
export class AppRoutingModule { }