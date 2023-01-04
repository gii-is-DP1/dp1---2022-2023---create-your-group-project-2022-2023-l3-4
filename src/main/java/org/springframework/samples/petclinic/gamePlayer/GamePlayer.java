package org.springframework.samples.petclinic.gamePlayer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.samples.petclinic.card.Card;
import org.springframework.samples.petclinic.model.BaseEntity;
import javax.persistence.OneToOne;
import org.springframework.samples.petclinic.player.Player;

import lombok.Getter;
import lombok.Setter;

/**
 * Simple business object representing a pet.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
@Getter
@Setter
@Entity
@Table(name = "gamePlayers")
public class GamePlayer extends BaseEntity {
	private Boolean winner;
	private Boolean host;

    @OneToOne
    private Player player;
    
    

    @OneToMany(mappedBy = "gamePlayer")
    private List<Card> cards;

    public List<Card> getBody(){
        return getCards().stream().filter(x->x.getBody()).collect(Collectors.toList());
    }
    public List<Card> getHand(){
        return getCards().stream().filter(x->!x.getBody() && x.getCardVaccine()==null && x.getCardVirus()==null).collect(Collectors.toList());
    }

    public Integer getNumHealthyOrgans(){
        return getBody().stream().filter(x->x.getVirus().size()==0).collect(Collectors.toList()).size();
    }

    public Boolean isWinner(){
       Integer numOrgansNeededToWin = 4; 
       return  getNumHealthyOrgans()==numOrgansNeededToWin;
    }

    public List<String> getBodyColours(){
        return getBody().stream().map(x->x.getType().getColour().toString()).collect(Collectors.toList());

    }
    public Boolean isThisOrganNotPresent(Card organ){
        Set<String> cards = new HashSet<>();
        cards.addAll(getBodyColours());
		cards.add(organ.getType().getColour().name());
		return cards.size()!=getBodyColours().size();
    }
}
