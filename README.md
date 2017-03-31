# SDIS-TrabalhosPrat

## Protocolos

### Backup
* Guardar path da localização do ficheiro a fazer backup no fileinfo ? Assim ao fazer reclaim space, quando o owner recebesse a mensagem poderia executar o chunkBackupProtocol passando o path do ficheiro automaticamente (ia busca-lo ao fileinfo dos storeds).
* ~~Ter um path default para os ficheiros locais. Exemplo: o meu input é image.png eu assumo que a pasta em que ele está é a pasta local dentro da pasta daquele Peer. O prof tem um exemplo assim nas especificações. Podemos facilmente identificar se o que ele nos dá é um path ou não basta verificar se existem '/'.~~
* Ficheiros com o mesmo nome e conteúdo diferente (versões diferentes) são tratados como ficheiros diferentes porque originam fileIds diferentes. Paragrafo 4, linha 4 da secção 2.1 diz que se se fizer backup a um ficheiro e já houver chunks que pertençam a um ficheiro com o mesmo nome então faz-se delete ao da versão mais antiga.
* Parag 7 da secção 3.2. Um peer que fez stored de um chunk deve guardar a contagem de stores do chunk (em memória não volátil). Assim depois é mais fácil escolher no reclaim space, os chunks a eliminar confirme o replication degree vs o replication degree desejado.
* Enhancemente ...

### Restore
* ~~Recovery feito por chunks: a confirmação também terá que ser por chunks. Este protocolo terá que ter uma estrutura igual ao do backup: reenvio das mensagens e etc POR CHUNK. À medida que se vai recebendo as confirmações põem se no record. O protocolo no trigger espera um tempo igual ao das 5 tentativas do reenvio das mensagens, se não receber todos os chunks então o protocolo falha! Isto é posssível no caso em que existam peers que não estejam ativos.~~
* É preciso limpar a lista de chunks restaurados de um peer (no record) no final. Suponto que se fazem dois restores do mesmo ficheiro, se se não se limpar o record de restores, um peer que contenha o chunk, na segunda vez que há o pedido, ele não o iria enviar porque no record há a informação de que já houveram outros peers a enviar o chunk (record do primeiro pedido).
* Enhancement -> criar um outro canal só para o envio desta informação particular ?

### Delete
* Enhancement -> Guardar no record os pedidos de delete que são válidos durante x tempo. Desta forma, se durante esse tempo houver um PUTCHUNK para esse ficheiro, o peer simplesmente o ignora.
* Ao fazer delete de um ficheiro, se esse ficheiro estiver nos restores do owner é preciso eliminar a entrada no hashmap para se no futuro fizer restore outra vez, não assumir que já se fez o restore.

### Space Reclaim
* Método de selecção dos chunks tendo em conta os replication degrees vs os desejados.
* Se entretanto for necessário executar o chunkBackupProtocol para um chunk eliminado, é necessario esperar x tempo para saber se um outro peer iniciou este mesmo protocolo.
* Atualizar a contagem dos chunks quando um peer recebe o remove.
* Enhancement -> Ponto 1 da secção Geral (abaixo) + Se não houver stores nem mais putchunks durante x tempo (então o peer que iniciou o protocolo pode ter sofrido um erro etc) então um peer inicia o protocolo de backupChunkProtocolo.

### Status
* Tudo

## Geral
* Todos os chunks guardarem a contagem do replication degree dos chunks. Ao atualizar esta informação no caso dos protocolos de reclaim ou delete, esperar x tempo e iniciar o chunkBackupProtocol se entretanto não receberem nenhum PUTCHUNK. Desta forma, restaura o replicationDegree.
* Implementar métodos para escolha de acções dependendo das versões.
* Demos para a apresentação.
* Considerar a possibilidade de implementar novos tipos de mensagens, apenas para a versão 2 do programa ( com enhancements ) se o caso assim o justificar.
* Se abrirmos os peers, abrir uma consola para o client e executar algo, fechar o client e depois abrir um outro, da segunda vez que se manda executar alguma coisa o initiator peer não recebe a informação do client.
