package com.time.tracealibility.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "irnss_data_view")
@Getter
@Setter
public class IrnssDataView {

    @Id
    private Long id;

    @Column(name = "mjd_date_time")
    private LocalDateTime mjdDateTime;
    private String satLetter;
    private Integer satNumber;

    private int sat;
    private String cl;
    private int mjd;
    private String sttime;
    private int trkl;
    private int elv;
    private int azth;
    private int refsv;
    private int srsv;
    private int refsys;
    private int srsys;
    private int dsg;
    private int ioe;
    private int mdtr;
    private int smdt;
    private int mdio;
    private int smdi;
    private int msio;
    private int smsi;
    private int isg;
    private int fr;
    private int hc;
    private String frc;
    private String ck;
    private String ionType;
    private String mode;
    private String source;

    @Override
    public String toString() {
        return "IrnssDataView{" +
                "id=" + id +
                ", mjdDateTime=" + mjdDateTime +
                ", satLetter='" + satLetter + '\'' +
                ", satNumber=" + satNumber +
                ", sat=" + sat +
                ", cl='" + cl + '\'' +
                ", mjd=" + mjd +
                ", sttime='" + sttime + '\'' +
                ", trkl=" + trkl +
                ", elv=" + elv +
                ", azth=" + azth +
                ", refsv=" + refsv +
                ", srsv=" + srsv +
                ", refsys=" + refsys +
                ", srsys=" + srsys +
                ", dsg=" + dsg +
                ", ioe=" + ioe +
                ", mdtr=" + mdtr +
                ", smdt=" + smdt +
                ", mdio=" + mdio +
                ", smdi=" + smdi +
                ", msio=" + msio +
                ", smsi=" + smsi +
                ", isg=" + isg +
                ", fr=" + fr +
                ", hc=" + hc +
                ", frc='" + frc + '\'' +
                ", ck='" + ck + '\'' +
                ", ionType='" + ionType + '\'' +
                ", mode='" + mode + '\'' +
                ", source='" + source + '\'' +
                '}';
    }

    public int getElv() {
        return elv;
    }

    public void setElv(int elv) {
        this.elv = elv;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getMjdDateTime() {
        return mjdDateTime;
    }

    public void setMjdDateTime(LocalDateTime mjdDateTime) {
        this.mjdDateTime = mjdDateTime;
    }

    public String getSatLetter() {
        return satLetter;
    }

    public void setSatLetter(String satLetter) {
        this.satLetter = satLetter;
    }

    public Integer getSatNumber() {
        return satNumber;
    }

    public void setSatNumber(Integer satNumber) {
        this.satNumber = satNumber;
    }

    public int getSat() {
        return sat;
    }

    public void setSat(int sat) {
        this.sat = sat;
    }

    public String getCl() {
        return cl;
    }

    public void setCl(String cl) {
        this.cl = cl;
    }

    public int getMjd() {
        return mjd;
    }

    public void setMjd(int mjd) {
        this.mjd = mjd;
    }

    public String getSttime() {
        return sttime;
    }

    public void setSttime(String sttime) {
        this.sttime = sttime;
    }

    public int getTrkl() {
        return trkl;
    }

    public void setTrkl(int trkl) {
        this.trkl = trkl;
    }

    public int getAzth() {
        return azth;
    }

    public void setAzth(int azth) {
        this.azth = azth;
    }

    public int getRefsv() {
        return refsv;
    }

    public void setRefsv(int refsv) {
        this.refsv = refsv;
    }

    public int getSrsv() {
        return srsv;
    }

    public void setSrsv(int srsv) {
        this.srsv = srsv;
    }

    public int getRefsys() {
        return refsys;
    }

    public void setRefsys(int refsys) {
        this.refsys = refsys;
    }

    public int getSrsys() {
        return srsys;
    }

    public void setSrsys(int srsys) {
        this.srsys = srsys;
    }

    public int getDsg() {
        return dsg;
    }

    public void setDsg(int dsg) {
        this.dsg = dsg;
    }

    public int getIoe() {
        return ioe;
    }

    public void setIoe(int ioe) {
        this.ioe = ioe;
    }

    public int getMdtr() {
        return mdtr;
    }

    public void setMdtr(int mdtr) {
        this.mdtr = mdtr;
    }

    public int getSmdt() {
        return smdt;
    }

    public void setSmdt(int smdt) {
        this.smdt = smdt;
    }

    public int getMdio() {
        return mdio;
    }

    public void setMdio(int mdio) {
        this.mdio = mdio;
    }

    public int getSmdi() {
        return smdi;
    }

    public void setSmdi(int smdi) {
        this.smdi = smdi;
    }

    public int getMsio() {
        return msio;
    }

    public void setMsio(int msio) {
        this.msio = msio;
    }

    public int getSmsi() {
        return smsi;
    }

    public void setSmsi(int smsi) {
        this.smsi = smsi;
    }

    public int getIsg() {
        return isg;
    }

    public void setIsg(int isg) {
        this.isg = isg;
    }

    public int getFr() {
        return fr;
    }

    public void setFr(int fr) {
        this.fr = fr;
    }

    public int getHc() {
        return hc;
    }

    public void setHc(int hc) {
        this.hc = hc;
    }

    public String getFrc() {
        return frc;
    }

    public void setFrc(String frc) {
        this.frc = frc;
    }

    public String getCk() {
        return ck;
    }

    public void setCk(String ck) {
        this.ck = ck;
    }

    public String getIonType() {
        return ionType;
    }

    public void setIonType(String ionType) {
        this.ionType = ionType;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
