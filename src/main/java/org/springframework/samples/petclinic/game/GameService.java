
package org.springframework.samples.petclinic.game;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.card.Card;
import org.springframework.samples.petclinic.card.CardService;
import org.springframework.samples.petclinic.card.GenericCard;
import org.springframework.samples.petclinic.card.GenericCardService;
import org.springframework.samples.petclinic.card.GenericCard.Type;
import org.springframework.samples.petclinic.gamePlayer.GamePlayer;
import org.springframework.samples.petclinic.gamePlayer.GamePlayerService;
import org.springframework.samples.petclinic.player.Player;
import org.springframework.samples.petclinic.room.Room;
import org.springframework.samples.petclinic.statistics.PlayerCount;
import org.springframework.samples.petclinic.statistics.WonPlayedGamesException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;

@Service
public class GameService {

	private GameRepository gameRepository;
	private CardService cardService;
	private GamePlayerService gamePlayerService;
	private GenericCardService genericCardService;

	@Autowired
	public GameService(GameRepository gameRepository, CardService cardService, GamePlayerService gamePlayerService, GenericCardService genericCardService) {
		this.gameRepository = gameRepository;
		this.cardService=cardService;
		this.gamePlayerService=gamePlayerService;
		this.genericCardService=genericCardService;
	}

	@Transactional(readOnly = true)
	public List<Game> listGames(){
		return gameRepository.findAll();
	}

	@Transactional(readOnly = true)
	public List<Game> findGamesByGameplayer(GamePlayer gamePlayer) {
		return gameRepository.findGamesByGameplayer(gamePlayer);
	}

	@Transactional(readOnly = true)
	public Page<Game> findGamesByGameplayerPaged(GamePlayer gamePlayer, Pageable page) {
		return gameRepository.findGamesByGameplayerPaged(gamePlayer, page);
	}
	
	@Transactional(readOnly = true)
	public Integer getNumGamesPlayed(GamePlayer p) {
		return gameRepository.numGamesPlayed(p);
	}

	@Transactional(readOnly = true)
	public Integer getNumGamesWon(GamePlayer p) {
		return gameRepository.numGamesWon(p);
	}

	@Transactional(readOnly = true)
	public List<PlayerCount> getRanking() {
		return gameRepository.playerRanking();
	}

	@Transactional(readOnly = true)
	public Integer getTotalGamesPlayed() {
		return gameRepository.totalGamesPlayed();
	}

	@Transactional(readOnly = true)
	public Collection<Game> listRunningGames(){
		return gameRepository.findRunningGames();
	}

	
	@Transactional(readOnly = true)
	public Collection<Game> listTerminateGames(){
		return gameRepository.findTerminategGames();
	}

	@Transactional(readOnly = true)
	public Collection<Game> findGameByRoomId(Integer roomId){
		return gameRepository.findGameByRoomId(roomId);
	}

	@Transactional
    public void deleteGame(Integer id) {
        gameRepository.deleteById(id);
    }

	@Transactional(readOnly = true)
	public Boolean isYourTurn(GamePlayer g, Integer gameId) {
		Game game = findGame(gameId);
		GamePlayer currentTurnGamePlayer = game.getGamePlayer().get(game.getTurn());
		Boolean isYourTurn = currentTurnGamePlayer.equals(g);
		return isYourTurn;
	}

	@Transactional(readOnly = true)
    public Game findGame(Integer i){
        return gameRepository.findById(i).get();
    }

	@Transactional(readOnly = true)
	public Game getRunningGame(Room room) {
		return gameRepository.findAnyRunningGame(room);
	}

	@Transactional
	public void save(Game game){
		gameRepository.save(game);

	}

	@Transactional(readOnly = true)
	public String humanReadableDuration(Duration d) {
		return d.toString().substring(2).replaceAll("(\\d[HMS])(?!$)", "$1 ").toLowerCase();
	}

	@Transactional(readOnly = true)
	public GamePlayer findGamePlayerByPlayer(Player player){
		return gameRepository.findGamePlayerByPlayer(player.getId());
	}
	//Si la baraja se queda sin cartas, se rellena con las ya descartadas
	public void fillDeck(Game g){
			List<Card> played_cards = g.discarted();
			cardService.shuffle(played_cards);
			List<Card> new_deck = new ArrayList<>();
			for(Card played_card: played_cards){
				played_card.setPlayed(false);
				new_deck.add(played_card);
			}
			g.setCards(played_cards);
			gameRepository.save(g);
		}
		
		//Barajar
		public void dealCards(Game game) {
				List<Card> baraja = game.baraja();
				for(GamePlayer jugador: game.getGamePlayer()) {
				List<Card> cartasJugador = jugador.getHand();
				while(cartasJugador.size()<3){
					if(baraja.size()==0) { //Si no quedan cartas en la baraja llamamos a fillDeck
						fillDeck(game);
						baraja = game.baraja();
					}
					Card card = baraja.get(0);
					cartasJugador.add(card); //Se la añadimos al jugador
					card.setGamePlayer(jugador);
					baraja.remove(0);	//La quitamos del mazo
					cardService.save(card);
				}
				jugador.setCards(cartasJugador);//Cuando ya tenga 3 cargas se guarda en el jugador
					gamePlayerService.save(jugador);
				}			
			
			}

			public void addOrgan(Card organ, GamePlayer gplayer1, GamePlayer gplayer2, ModelMap model){
				if(gplayer2.isThisOrganNotPresent(organ)){
					gplayer1.getCards().remove(organ);
					organ.setGamePlayer(gplayer2);
					organ.setBody(true);
					gplayer2.getCards().add(organ);
					
					
				}else{
					throw new IllegalArgumentException("You can't add two " + organ.getType().getColour() + " organs to a body");		
				}			
		}


			public void changeCards(GamePlayer g1, GamePlayer g2, Card c_organ1, Card c_organ2){
				if(c_organ1.getType().getType().toString()=="ORGAN"
				&& c_organ2.getType().getType().toString()=="ORGAN"){
				if(c_organ1.getVaccines().size()<2 && c_organ2.getVaccines().size()<2){
				if((g1.isThisOrganNotPresent(c_organ2) && g2.isThisOrganNotPresent(c_organ1)) || c_organ1.getType().getColour()==c_organ2.getType().getColour()){
					cardService.changeGamePlayer(c_organ1, g1, g2);
					cardService.changeGamePlayer(c_organ2, g2, g1);
												
			}else{
				throw new IllegalArgumentException("A body can't have repeated organs.");
			}
			}else{
				throw new IllegalArgumentException("You can't exchange immunized organs.");
				
			}
		}else{
		throw new IllegalArgumentException("You can only exchange organs.");
	}
			}
		


		public void changeTurn(Game game)	{
			if(game.getTurn()==game.getGamePlayer().size()-1){ //Si es el último jugador
				game.setTurn(0); //Cambiamos el turno a 0
				game.setRound(game.getRound()+1); //Añadimos una ronda
				dealCards(game);} //Y repartimos cartas
			else{game.setTurn(game.getTurn()+1); //Sino solo incrementamos el turno en 1
			}
			gameRepository.save(game); //Guardamos los cambios de game
		}

		private void achus(Card virus, Card organ, GamePlayer gamePlayer1, GamePlayer gamePlayer2){
			cardService.changeGamePlayer(virus, gamePlayer1, gamePlayer2);
			virus.setCardVirus(organ);
			organ.getVirus().add(virus);
		}


        public void thief(Card thiefCard, GamePlayer thiefPlayer, GamePlayer victimPlayer, Card stolenCard) {
				// Realizamos el robo de la carta
				if(thiefPlayer.isThisOrganNotPresent(stolenCard)){
					if(stolenCard.getVaccines().size()<2){
						thiefPlayer.getCards().remove(thiefCard);
						thiefCard.discard();
						cardService.changeGamePlayer(stolenCard, victimPlayer, thiefPlayer);
				}else{
					throw new IllegalArgumentException("You can't stole an immune organ");
				}
				} else{
					throw new IllegalArgumentException("You can't have repeated organs");
				}
		}
	public void infection(GamePlayer gamePlayer1, GamePlayer gamePlayer2){
		Card special_used = gamePlayer1.getHand().stream().filter(c->c.getType().getType()==Type.INFECTION).findFirst().get();
		gamePlayer1.getCards().remove(special_used);
		special_used.discard();
		List<Card> virusInTheBody = gamePlayer1.getVirusInTheBody();
		List<Card> cleanOrgans = gamePlayer2.getCleanOrgans();
		
		for (Card virus : virusInTheBody) {
		//Comprobar si se pueden infectar sus organos
			for (Card organ : cleanOrgans) {
				if (organ.getType().getColour() == virus.getType().getColour()) {
					achus(virus,organ,gamePlayer1,gamePlayer2);
				}
			}
		}
		virusInTheBody = gamePlayer1.getVirusInTheBody();
		cleanOrgans = gamePlayer2.getCleanOrgans();
		
		while(virusInTheBody.size()>0 && cleanOrgans.size()>0 &&(cardService.getColours(virusInTheBody).contains("RAINBOW")
		|| cardService.getColours(cleanOrgans).contains("RAINBOW"))) {
				if(cardService.getColours(virusInTheBody).contains("RAINBOW")){
					Card virus = cardService.getARainBow(virusInTheBody);
					achus(virus,cleanOrgans.get(0),gamePlayer1,gamePlayer2);
					virusInTheBody.remove(virus);
					cleanOrgans.remove(cleanOrgans.get(0));
				}else if(cardService.getColours(cleanOrgans).contains("RAINBOW")){
					Card organ = cardService.getARainBow(cleanOrgans);
					achus(virusInTheBody.get(0),organ,gamePlayer1,gamePlayer2);
					virusInTheBody.remove(virusInTheBody.get(0));
					cleanOrgans.remove(organ);
				}
			} 
		}
	

	public void glove(GamePlayer gamePlayer, Game game) {
		for (GamePlayer otherGamePlayer : game.getGamePlayer()) {
			if (otherGamePlayer != gamePlayer) {  // Excluimos al jugador que ejecuta la acción
				for(Card c: otherGamePlayer.getHand()) {
					c.discard();
					cardService.save(c);
				}
				otherGamePlayer.setCards(new ArrayList<>());  // Descartamos todas las cartas del mazo del jugador
			}
		}
	}

	public void medicalError(GamePlayer gamePlayer1, GamePlayer gamePlayer2) {
		List<Card> player1Cards = gamePlayer1.getBody();
		List<Card> player2Cards = gamePlayer2.getBody();
		for(Card c: player1Cards) {
			c.setGamePlayer(gamePlayer2);
			gamePlayer1.getCards().remove(c);
		}
		
		for(Card c: player2Cards) {
			c.setGamePlayer(gamePlayer1);
			gamePlayer2.getCards().remove(c);
		}
		gamePlayer1.getCards().addAll(player2Cards);
		gamePlayer2.getCards().addAll(player1Cards);
	}
		

	public List<GamePlayer> classificate(List<GamePlayer> gamePlayers){
		Collections.sort(gamePlayers, Comparator.comparingInt(GamePlayer::getNumHealthyOrgans).reversed());
		return gamePlayers;
	}
	
	@Transactional(readOnly = false)
	public Game startGame(Room room) {
		Game game = new Game();
		game.setRoom(room);
		game.setIsRunning(true);
		game.setRound(0);
		game.setTurn(0);
		game.setInitialHour(LocalDateTime.now());
		game.setIsRunning(true);
		List<GamePlayer> gamePlayers = new ArrayList<>();
		game.setCards(new ArrayList<>());
		List<Player> players = new ArrayList<>(room.getPlayers());
		
		for(Player p: players) {
			GamePlayer gp = findGamePlayerByPlayer(p);
			gp.setCards(new ArrayList<>());
			gamePlayers.add(gp);
			gamePlayerService.save(gp);
		}

		game.setGamePlayer(gamePlayers);
		List<GenericCard> deck = genericCardService.findGCards();
		for(GenericCard c: deck){
			Card card = new Card();
			card.setType(c);
			card.setBody(false);
			card.setPlayed(false);
			card.setVaccines(new ArrayList<>());
			card.setVirus(new ArrayList<>());
			game.getCards().add(card);
			cardService.save(card);
		}
		Collections.shuffle(game.getCards());
		save(game);
		dealCards(game);

		return game;
	}

	@Transactional(readOnly = false)
	public void discard(List<Card> cards, GamePlayer gamePlayer) {
			for(Card card: cards){	//Recorremos las cartas que quiere descartar					
					gamePlayer.getCards().remove(card); //Cada carta la quitamos de la lista de cartas del jugador
					card.discard();
					cardService.save(card);	//Se guarda la carta	
			}  
			gamePlayerService.save(gamePlayer); //Cuando ya se han eliminado todas, se guarda el jugador	
	}

	@Transactional(readOnly = false)
	public void finishGame(Game game){
		game.endGame();
		List<GamePlayer> classification = classificate(game.getGamePlayer());
		game.setGamePlayer(classification);
		game.setWinner(game.getGamePlayer().stream().filter(g -> g.isWinner()).findFirst().get());
    	game.getCards().stream().forEach(c -> {
			c.setGamePlayer(null);
			cardService.save(c);
		} );
    
		save(game);
	}
}