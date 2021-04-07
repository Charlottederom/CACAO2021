package abstraction.eq7Distributeur2;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import abstraction.fourni.*;
import abstraction.eq8Romu.produits.ChocolatDeMarque;
import abstraction.fourni.Filiere;
import abstraction.fourni.IActeur;
import abstraction.fourni.Journal;
import abstraction.fourni.Variable;
import abstraction.eq8Romu.contratsCadres.*;


public class Achat extends Distributeur2Acteur implements IAcheteurContratCadre {
	
	private Distributeur2Acteur wonka;
	private HashMap<ChocolatDeMarque, Variable> besoinsChoco;
	private double quantiteLimite, quantiteMax;
	private LinkedList<ExemplaireContratCadre> contrats;
	private SuperviseurVentesContratCadre supCCadre;
	

	public Achat(Distributeur2Acteur wonka) {
		this.wonka = wonka;
		this.besoinsChoco = new HashMap<ChocolatDeMarque,Variable>();
		this.quantiteLimite =  10; //arbitrairement choisie : pas descendre en dessous de cette quantité pour n'importe quel produit
		this.quantiteMax = 40;//arbitrairement choisie : quantité max pour limiter les coûts de stockage
		this.supCCadre = (SuperviseurVentesContratCadre) Filiere.LA_FILIERE.getActeur("supCCadre");
	}
	public void majDemande() {
		//crée un tableau avec la quantité qu'on doit commander pour chaque chocolat
		for(ChocolatDeMarque choco : Filiere.LA_FILIERE.getChocolatsProduits()) {
			if(stocks.getStockChocolatDeMarque(choco) <= quantiteLimite) {
				besoinsChoco.put(choco, new Variable("Quantité", wonka, quantiteMax - stocks.getStockChocolatDeMarque(choco)));
			}
			else {
				besoinsChoco.put(choco, new Variable("Quantité", wonka, 0));
			}
		}		
	}
	
	//cherche des nouveaux contrats cadres pour tous les chocolats dont le stock est inférieur à quantiteLimite
	public void nouveauContrat() {
		for(ChocolatDeMarque choco : Filiere.LA_FILIERE.getChocolatsProduits() ) {
			LinkedList<IVendeurContratCadre> vendeurs = (LinkedList<IVendeurContratCadre>) supCCadre.getVendeurs(choco);
			int i = (int) (Math.random()*vendeurs.size()) ;
			IVendeurContratCadre vendeur = vendeurs.get(i);
			Echeancier echeancier = new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 10, besoinsChoco.get(choco).getValeur()/10);
			supCCadre.demande((IAcheteurContratCadre)wonka, vendeur, choco, echeancier, wonka.getCryptogramme(), false);
		}
	}
	
	public Echeancier contrePropositionDeLAcheteur(ExemplaireContratCadre contrat) {
		Echeancier e = contrat.getEcheancier();
		if(e.getNbEcheances()>=10) { //si l'échéancier est réparti sur plus de 10 étapes : trop long (arbitraire)
			//on rajoute à l'étape 0 toutes les quantités qui auraient du être livrées après l'étape 10
			e.set(e.getStepDebut(), e.getQuantiteAPartirDe(10)+e.getQuantite(0));
			for(int i=10; i<e.getNbEcheances(); i++) {
				//pour chaque étape au dessus de 10, on met quantité = 0
				e.set(i, 0);
			}
			return e;
		}
		//si la quantité proposée par le vendeur est inférieure à la quantité voulue
		if(e.getQuantiteTotale()<besoinsChoco.get(contrat.getProduit()).getValeur()) {
			e.set(0, e.getQuantite(0)+(besoinsChoco.get(contrat.getProduit()).getValeur()-e.getQuantiteTotale()));
			return e;
		}
		else { return e;
		}
	}

	
	public double contrePropositionPrixAcheteur(ExemplaireContratCadre contrat) {
		double prix = contrat.getListePrix().get(contrat.getListePrix().size()-1);
		if(wonka.getAutorisationTransaction(prix)) {
			return contrat.getPrix();
		}
		else {
			return contrat.getPrix()*0.90;
		}
	}


	public void receptionner(Object produit, double quantite, ExemplaireContratCadre contrat) {
		stocks.ajouterChocolatDeMarque((ChocolatDeMarque) contrat.getProduit(), contrat.getQuantiteTotale());
		
		
	}



}