# SDIS-TrabalhosPrat

## Protocolos

### Backup
* descomentar o enhancement

### Restore
* Enhancement : If chunks are large, this protocol may not be desirable: only one peer needs to receive the chunk, but we are using a multicast channel for sending the chunk. Can you think of a change to the protocol that would eliminate this problem, and yet interoperate with non-initiator peers that implement the protocol described in this section?
* criar um outro canal só para o envio desta informação particular ? -> alterar o getchunk com um campo que é um endereço particular que abre outra ligação entre o peer que vai mandar um chunk e aquele que pediu. Depois envia-se uma mensagem para multicast de confirmação da recepção. (seria preciso aumentar o randomTime de espera)

### Delete
* Enhancement: If a peer that backs up some chunks of the file is not running at the time the initiator peer sends a DELETE message for that file, the space used by these chunks will never be reclaimed. Can you think of a change to the protocol, possibly including additional messages, that would allow to reclaim storage space even in that event?
* Solution -> Quando um peer se conecta, manda um tipo de mensagens especiais, e passa o fileId de cada tipo de ficheiros que tem. Quem recebe, se ainda tiver chunks desse fileId então manda uma mensagem de volta. Se ninguem respoder, ou se o repDegree/Desired for muito baixo então há a probabilidade daquele peer também ter sido iniciado a pouco tempo e portanto ignora-o e elimina os chunks em backup daquele ficheiro.

### Space Reclaim

### State

## Geral
* Implementar métodos para escolha de acções dependendo das versões.
* Demos para a apresentação.

### Outros
* Considerar a possibilidade de implementar novos tipos de mensagens, apenas para a versão 2 do programa ( com enhancements ) se o caso assim o justificar.
* Por todos os prints para os logs
* Documentação
* Relatório
