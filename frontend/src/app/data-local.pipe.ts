import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'dataLocal' })
export class DataLocalPipe implements PipeTransform {
  transform(valor: string): string {
    // O backend retorna horário local, sem fuso: apenas formatar, sem criar Date.
    const [data, hora] = valor.split('T');
    return `${data.split('-').reverse().join('/')} ${hora}`;
  }
}
