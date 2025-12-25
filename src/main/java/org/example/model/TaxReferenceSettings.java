package org.example.model;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class TaxReferenceSettings {
    /** Наименование клиники — берётся из practice_locations.description */
    private String clinicName = "";

    /** ИНН медицинской организации — из practice_locations.tax_file_no */
    private String inn = "";

    /** КПП — из practice_locations.medicare_prov_no */
    private String kpp = "";
    private String creatorFullName = "Иванов Иван Иванович";
    private int copiesCount = 2;
    private String taxOrgCode = "1234";
    private int orgType = 1; // 1=Организация, 2=ИП
    private int signerType = 1; // 1=Руководитель, 2=Представитель
    private String documentName = "";
    private String ecpSignerName = "";
    private String exportPath = "D:\\Файлы\\Файлы пациентов";
    private int procedureType = 1; // 1 или 2
    private Patient selectedPatient;
    private String selectedPractice = "Все филиалы";

    /** Номер справки — уникальный, от 1 до N (см. п. 18 Приказа ФНС) */
    private String referenceNumber = "1";

    private boolean paperCarrier = true;  // по умолчанию — бумажный
    private boolean fileExport = false;   // по умолчанию — файл не нужен


    private Set<String> selectedCategories = new HashSet<>();
    private Set<String> procedureCategories = new LinkedHashSet<>();

    // Getters and setters
    public String getClinicName() { return clinicName; }
    public void setClinicName(String clinicName) { this.clinicName = clinicName; }

    public String getInn() { return inn; }
    public void setInn(String inn) { this.inn = inn; }

    public String getKpp() { return kpp; }
    public void setKpp(String kpp) { this.kpp = kpp; }

    public String getCreatorFullName() { return creatorFullName; }
    public void setCreatorFullName(String creatorFullName) { this.creatorFullName = creatorFullName; }

    public int getCopiesCount() { return copiesCount; }
    public void setCopiesCount(int copiesCount) { this.copiesCount = copiesCount; }

    public String getTaxOrgCode() { return taxOrgCode; }
    public void setTaxOrgCode(String taxOrgCode) { this.taxOrgCode = taxOrgCode; }

    public int getOrgType() { return orgType; }
    public void setOrgType(int orgType) { this.orgType = orgType; }

    public int getSignerType() { return signerType; }
    public void setSignerType(int signerType) { this.signerType = signerType; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public String getEcpSignerName() { return ecpSignerName; }
    public void setEcpSignerName(String ecpSignerName) { this.ecpSignerName = ecpSignerName; }

    public String getExportPath() { return exportPath; }
    public void setExportPath(String exportPath) { this.exportPath = exportPath; }

    public int getProcedureType() { return procedureType; }
    public void setProcedureType(int procedureType) { this.procedureType = procedureType; }

    public Patient getSelectedPatient() { return selectedPatient; }
    public void setSelectedPatient(Patient selectedPatient) { this.selectedPatient = selectedPatient; }



    public boolean isPaperCarrier() { return paperCarrier; }
    public void setPaperCarrier(boolean paperCarrier) { this.paperCarrier = paperCarrier; }

    public boolean isFileExport() { return fileExport; }
    public void setFileExport(boolean fileExport) { this.fileExport = fileExport; }

    public Set<String>getProcedureCategories(){ return new LinkedHashSet<>(procedureCategories); } // возвращаем копию для безопасности

    public String getSelectedPractice() {
        return selectedPractice;
    }

    public void setSelectedPractice(String selectedPractice) {
        this.selectedPractice = selectedPractice != null ? selectedPractice : "Все филиалы";
    }

    public void setProcedureCategories(Set<String> procedureCategories) {
        this.procedureCategories.clear();
        if (procedureCategories != null) {
            this.procedureCategories.addAll(procedureCategories);
        }
    }

    public void addProcedureCategory(String category) {
        if (category != null && !category.trim().isEmpty()) {
            this.procedureCategories.add(category.trim());
        }
    }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public void clearProcedureCategories() {
        this.procedureCategories.clear();
    }

    public Set<String> getSelectedCategories() {
        return selectedCategories;
    }

    public void setSelectedCategories(Set<String> selectedCategories) {
        this.selectedCategories.clear();
        if (selectedCategories != null) {
            this.selectedCategories.addAll(selectedCategories);
        }
    }

    public void addCategory(String category) {
        selectedCategories.add(category);
    }

    public void removeCategory(String category) {
        selectedCategories.remove(category);
    }
}