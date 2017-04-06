# SDIS-TrabalhosPrat

## Protocolos

### Backup

### Restore
* É preciso limpar a lista de chunks restaurados de um peer (no record) no final. Suponto que se fazem dois restores do mesmo ficheiro, se se não se limpar o record de restores, um peer que contenha o chunk, na segunda vez que há o pedido, ele não o iria enviar porque no record há a informação de que já houveram outros peers a enviar o chunk (record do primeiro pedido).
* Enhancement -> criar um outro canal só para o envio desta informação particular ? -> alterar o getchunk com um campo que é um endereço particular que abre outra ligação entre o peer que vai mandar um chunk e aquele que pediu. Depois envia-se uma mensagem para multicast de confirmação da recepção. (seria preciso aumentar o randomTime de espera)
* Se se eliminar um ficheiro, ele também será eliminado caso esteja nos restores?

### Delete
* Enhancement -> De x em x tempo (thread com runnable la dentro) -> executer, ele vai buscar 1 só chunks por cada ficheiro e envia uma mensagem para multicast a perguntar se não foi eliminado os chunks desse ficheiro. O owner peer desse ficheiro enviará uma mensagem de resposta em caso de sucesso. Caso x tempo depois de perguntar, o peer não obtenha resposta, elimina todos os chunks do ficheiro. Isto é util caso o peer esteja em baixo quando haja a eliminação de chunks de ficheiros. 

### Space Reclaim
* Método de selecção dos chunks tendo em conta os replication degrees vs os desejados. Vai buscar os que têm maior RepDegree por ordem decrescente e começa a eliminar esses. Caso ainda seja preciso eliminar mais, então elimina os restantes necessários sem qualquer cuidado prévio.
* Enhancement -> Ponto 1 da secção Geral (abaixo) + Se não houver stores nem mais putchunks durante x tempo (então o peer que iniciou o protocolo pode ter sofrido um erro etc) então um peer inicia o protocolo de backupChunkProtocolo.

### State

## Geral
* De x em x tempo thread (com runnable la dentro) -> executer, vai buscar todos os chunks com repDegree abaixo do desejado e inicia o chunkBackupProtocol para cada um deles após xDelay e caso, se durante esse tempo de espera, não receber nenhum putchunk para o chunk em específico. Isto é util quando o chunkBackup protocol falha a meio ou então não consegue garantir a replication degree dentro das tentativas possíveis.
* Implementar métodos para escolha de acções dependendo das versões.
* Demos para a apresentação.
* Considerar a possibilidade de implementar novos tipos de mensagens, apenas para a versão 2 do programa ( com enhancements ) se o caso assim o justificar.
* Receber feedback dos peers de acordo com o sucesso/insucesso da execução dos protocolos e enviar essa informação para o client.
