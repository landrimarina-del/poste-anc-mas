package it.poste.anc.bpmgw.inbound;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BpmPracticeOpenRequest {

    @JsonProperty("CANALE")
    private String canale;

    @JsonProperty("ID_WORKITEM")
    private String idWorkItem;

    @JsonProperty("NUM_PRATICA")
    @JsonAlias({"REQUEST_ID", "APPIAN_TICKET_ID"})
    private String numPratica;

    @JsonProperty("CF_CLIENTE")
    private String cfCliente;

    @JsonProperty("CODICE_CLIENTE")
    private String codiceCliente;

    @JsonProperty("DATA_INSERIMENTO_RICHIESTA")
    private String dataInserimentoRichiesta;

    @JsonProperty("CLIENTE")
    private ClientData cliente;

    @JsonProperty("DATI_CARTA_BLOCCATA")
    private CardData datiCartaBloccata;

    @JsonProperty("DOCUMENTI")
    private List<DocumentData> documenti;

    public String getCanale() {
        return canale;
    }

    public String getIdWorkItem() {
        return idWorkItem;
    }

    public String getNumPratica() {
        return numPratica;
    }

    public String getCfCliente() {
        return cfCliente;
    }

    public String getCodiceCliente() {
        return codiceCliente;
    }

    public String getDataInserimentoRichiesta() {
        return dataInserimentoRichiesta;
    }

    public ClientData getCliente() {
        return cliente;
    }

    public CardData getDatiCartaBloccata() {
        return datiCartaBloccata;
    }

    public List<DocumentData> getDocumenti() {
        return documenti;
    }

    public static class ClientData {
        @JsonProperty("COGNOME")
        private String cognome;

        @JsonProperty("NOME")
        private String nome;

        @JsonProperty("CODICE_FISCALE")
        private String codiceFiscale;

        @JsonProperty("DOCUMENTO_TIPO")
        private String documentoTipo;

        @JsonProperty("DOCUMENTO_NUM")
        private String documentoNum;

        @JsonProperty("DATA_NASCITA")
        @JsonAlias("DATANASCITA")
        private String dataNascita;

        @JsonProperty("SESSO")
        private String sesso;

        @JsonProperty("COMUNENASCITA")
        private String comuneNascita;

        @JsonProperty("PROVINCIANASCITA")
        private String provinciaNascita;

        @JsonProperty("NAZIONENASCITA")
        private String nazioneNascita;

        @JsonProperty("CITTADINANZA")
        private String cittadinanza;

        @JsonProperty("CELLULARE")
        private String cellulare;

        @JsonProperty("TELEFONO")
        private String telefono;

        @JsonProperty("INDIRIZZO_DI_RESIDENZA")
        private ResidenceAddressData indirizzoDiResidenza;

        public String getCognome() {
            return cognome;
        }

        public String getNome() {
            return nome;
        }

        public String getCodiceFiscale() {
            return codiceFiscale;
        }

        public String getDocumentoTipo() {
            return documentoTipo;
        }

        public String getDocumentoNum() {
            return documentoNum;
        }

        public String getDataNascita() {
            return dataNascita;
        }

        public String getSesso() {
            return sesso;
        }

        public String getComuneNascita() {
            return comuneNascita;
        }

        public String getProvinciaNascita() {
            return provinciaNascita;
        }

        public String getNazioneNascita() {
            return nazioneNascita;
        }

        public String getCittadinanza() {
            return cittadinanza;
        }

        public String getCellulare() {
            return cellulare;
        }

        public String getTelefono() {
            return telefono;
        }

        public ResidenceAddressData getIndirizzoDiResidenza() {
            return indirizzoDiResidenza;
        }
    }

    public static class ResidenceAddressData {
        @JsonProperty("LUOGO")
        private String luogo;

        @JsonProperty("COMUNE")
        private String comune;

        @JsonProperty("PROVINCIA")
        private String provincia;

        @JsonProperty("NAZIONE")
        private String nazione;

        @JsonProperty("CAP")
        private String cap;

        @JsonProperty("CIVICO")
        private String civico;

        public String getLuogo() {
            return luogo;
        }

        public String getComune() {
            return comune;
        }

        public String getProvincia() {
            return provincia;
        }

        public String getNazione() {
            return nazione;
        }

        public String getCap() {
            return cap;
        }

        public String getCivico() {
            return civico;
        }
    }

    public static class CardData {
        @JsonProperty("I_NUMERO_CARTA")
        private String numeroCarta;

        @JsonProperty("I_TIPO_CARTA")
        private String tipoCarta;

        @JsonProperty("I_INTEST_CARTA")
        private String intestatarioCarta;

        public String getNumeroCarta() {
            return numeroCarta;
        }

        public String getTipoCarta() {
            return tipoCarta;
        }

        public String getIntestatarioCarta() {
            return intestatarioCarta;
        }
    }

    public static class DocumentData {
        @JsonProperty("CODICE_DOC_ID")
        private Integer codiceDocId;

        @JsonProperty("CONTENUTI")
        private List<DocumentContentData> contenuti;

        public Integer getCodiceDocId() {
            return codiceDocId;
        }

        public List<DocumentContentData> getContenuti() {
            return contenuti;
        }
    }

    public static class DocumentContentData {
        @JsonProperty("NOME_FILE")
        private String nomeFile;

        @JsonProperty("ESTENSIONE")
        private String estensione;

        @JsonProperty("ID_DOC")
        private String idDoc;

        @JsonProperty("LINKDOWNLOAD")
        private String linkDownload;

        public String getNomeFile() {
            return nomeFile;
        }

        public String getEstensione() {
            return estensione;
        }

        public String getIdDoc() {
            return idDoc;
        }

        public String getLinkDownload() {
            return linkDownload;
        }
    }
}