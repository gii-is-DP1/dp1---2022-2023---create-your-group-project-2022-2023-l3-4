package org.springframework.samples.petclinic.game;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.samples.petclinic.card.Card;
import org.springframework.samples.petclinic.card.CardService;
import org.springframework.samples.petclinic.card.GenericCard;
import org.springframework.samples.petclinic.card.Hand;
import org.springframework.samples.petclinic.card.GenericCard.Colour;
import org.springframework.samples.petclinic.card.GenericCard.Type;
import org.springframework.samples.petclinic.gamePlayer.GamePlayer;
import org.springframework.samples.petclinic.configuration.SecurityConfiguration;
import org.springframework.samples.petclinic.gamePlayer.GamePlayerService;
import org.springframework.samples.petclinic.player.Player;
import org.springframework.samples.petclinic.room.Room;
import org.springframework.samples.petclinic.room.RoomService;
import org.springframework.samples.petclinic.statistics.WonPlayedGamesException;
import org.springframework.samples.petclinic.util.AuthenticationService;
import org.springframework.ui.ModelMap;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.test.context.support.WithMockUser;


@WebMvcTest(controllers = GameController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, 
classes = WebSecurityConfigurer.class), excludeAutoConfiguration = SecurityConfiguration.class)
public class GameControllerTest {

		@MockBean
		private GameService gameService;
		
		@MockBean
		private RoomService roomService;
		
		@MockBean
		private GamePlayerService gamePlayerService;
		
		@MockBean
		private CardService cardService;
		
		@MockBean
		private AuthenticationService authenticationService;
		
		
		@Autowired
		private GameController gameController;
		

		@BeforeEach
		public void init() {
			MockitoAnnotations.initMocks(this);
		}
		
		@Test
		public void testInit() {
			//given
			Integer roomId = 1;
			Integer gameId = 0;
			Room room = new Room();
			Game runningGame = new Game();
			Player currentPlayer = new Player();
			GamePlayer gp_vista = new GamePlayer();
			ModelMap model = new ModelMap();
			runningGame.setId(0);
			runningGame.setWinner(null);
			Card card = new Card();
			card.setBody(false);
			gp_vista.setCards(List.of(card));
			card.setGamePlayer(gp_vista);
			runningGame.setGamePlayer(List.of(gp_vista));
			runningGame.setTurn(0);
			room.setHost(currentPlayer);
			
			//when
			when(authenticationService.getGamePlayer()).thenReturn(gp_vista);
			when(roomService.findRoomById(roomId)).thenReturn(room);
			when(gameService.getRunningGame(room)).thenReturn(null);
			when(gameService.findGame(gameId)).thenReturn(runningGame);
			when(gameService.startGame(room)).thenReturn(runningGame);
			when(authenticationService.getPlayer()).thenReturn(currentPlayer);
			
			//then
			String result = gameController.init(roomId, model);
			assertThat(result).isEqualTo("redirect:/games/"+ runningGame.getId());
		}
		
		@Test
		public void testListRunningGames() {
			//given
			Collection<Game> runningGames = new ArrayList<>();
			ModelMap model = new ModelMap();
			
			//when
			when(gameService.listRunningGames()).thenReturn(runningGames);
			
			//then
			String result = gameController.listRunningGames(model);
			assertThat(result).isEqualTo(GameController.RUNNING_GAMES_LISTING);
			assertThat(model.get("games")).isEqualTo(runningGames);
		}
		
		@Test
		public void testTerminateRunningGames() {
			//given
			Collection<Game> terminateGames = new ArrayList<>();
			ModelMap model = new ModelMap();
			
			//when
			when(gameService.listTerminateGames()).thenReturn(terminateGames);
			
			//then
			String result = gameController.terminateRunningGames(model);
			assertThat(result).isEqualTo(GameController.TERMINATE_GAMES_LISTING);
			assertThat(model.get("games")).isEqualTo(terminateGames);
		}
		
		@Test
		public void testMuestraVista() {
			//given
			int gameId = 1;
			GamePlayer gp_vista = new GamePlayer();
			Game game = new Game();
			ModelMap model = new ModelMap();
			Card card = new Card();
			card.setBody(false);
			gp_vista.setCards(List.of(card));
			card.setGamePlayer(gp_vista);
			game.setGamePlayer(List.of(gp_vista));
			game.setTurn(0);

			//when
			when(gameService.findGame(gameId)).thenReturn(game);
			when(authenticationService.getGamePlayer()).thenReturn(gp_vista);
			
			//then
			String result = gameController.muestraVista(gameId, model);
			assertThat(result).isEqualTo("games/game");
			assertThat(model.get("hand")).isEqualTo(gp_vista.getHand());
			assertThat(model.get("bodies")).isInstanceOf(Map.class);
			assertThat(model.get("isYourTurn")).isInstanceOf(Boolean.class);
			assertThat(model.get("currentTurnGamePlayer")).isInstanceOf(GamePlayer.class);
		}
		
    @WithMockUser
    @Test
	public void testGeneraTablero() {
		ModelMap model = new ModelMap();
		GamePlayer gamePlayer = new GamePlayer();
		Game game = new Game();
        Card card = new Card();
		card.setBody(false);
        gamePlayer.setCards(List.of(card));
        card.setGamePlayer(gamePlayer);
        game.setGamePlayer(List.of(gamePlayer));
		
		ModelMap result = gameController.generaTablero(model, gamePlayer, game);
		
		assertTrue(result.containsKey("hand"));
		assertTrue(result.containsKey("bodies"));
		assertEquals(gamePlayer.getHand(), result.get("hand"));
	}
	
	@Test
	public void testTurn() {
		//given
		int gameId = 1;
		Game game = new Game();
		game.setId(1);
		GamePlayer gamePlayer = new GamePlayer();
        game.setGamePlayer(List.of(gamePlayer));
		Card card = new Card();
        GenericCard genericCard = new GenericCard();
        genericCard.setId(1);
        genericCard.setType(Type.ORGAN);
        genericCard.setColour(Colour.RED);
		card.setType(genericCard);
		card.setBody(true);
		gamePlayer.setCards(List.of(card));
		card.setGamePlayer(gamePlayer);
		
		//when
		when(gameService.findGame(gameId)).thenReturn(game);
		
		//then
		String result = gameController.turn(gameId);
		assertThat(result).isEqualTo("redirect:/games/" + game.getId());
	}

    @Test
	public void testPlay() {
		//given
		int gameId = 1;
		Integer cardId = 1;
		Card card = new Card();
		GamePlayer gp = new GamePlayer();
		Game game = new Game();
		ModelMap model = new ModelMap();
        GenericCard genericCard = new GenericCard();
        genericCard.setId(1);
        genericCard.setType(Type.ORGAN);
        genericCard.setColour(Colour.RED);
		card.setType(genericCard);
		card.setBody(false);
		gp.setCards(List.of(card));
		card.setGamePlayer(gp);
		game.setGamePlayer(List.of(gp));
		
		//when
		when(cardService.findCard(cardId)).thenReturn(Optional.of(card));
		when(authenticationService.getGamePlayer()).thenReturn(gp);
		when(gameService.findGame(gameId)).thenReturn(game);
		
		//then
		assertEquals("/games/selecciona", gameController.play(gameId, cardId, model));
	}
	
	@Test
	public void testPlayOnBody() {
		//given
		Integer gameId = 1;
		Integer cardId = 1;
		Integer g_id = 1;
		GamePlayer gp = new GamePlayer();
		Card card = new Card();
		Game game = new Game();
		game.setId(gameId);
        GenericCard genericCard = new GenericCard();
        genericCard.setId(1);
        genericCard.setType(Type.ORGAN);
        genericCard.setColour(Colour.RED);
		card.setType(genericCard);
		card.setGamePlayer(gp);
		gp.setCards(List.of(card));
		gp.setId(g_id);
		List<GamePlayer> gps = new ArrayList<>();
		gps.add(gp);
		game.setGamePlayer(gps);
		ModelMap model = new ModelMap();
		
		//when
		when(authenticationService.getGamePlayer()).thenReturn(gp);
		when(gamePlayerService.findById(g_id)).thenReturn(Optional.of(gp));
		when(cardService.findCard(cardId)).thenReturn(Optional.of(card));
		when(gameService.findGame(gameId)).thenReturn(game);
		
		//then
		assertEquals("playOrgan", gameController.playOnBody(gameId, cardId, gp.getId(), model));
	}
	
	@Test
	public void testPlayOnCard() {
		//given
		Game game = new Game();
		game.setId(1);
		Integer cardId = 1;
		Integer targetC = 1;
		Card card = new Card();
        GenericCard genericCard = new GenericCard();
        genericCard.setId(1);
        genericCard.setType(Type.VACCINE);
        genericCard.setColour(Colour.RED);
		card.setType(genericCard);
		ModelMap model = new ModelMap();
		
		//when
		when(cardService.findCard(cardId)).thenReturn(Optional.of(card));
		
		//then
		assertEquals("playVaccine", gameController.playOnCard(game.getId(), cardId, targetC, model));
	}
	
	@Test
	public void testPlayOnAnotherCard() {
		//given
		Integer gameId = 1;
		Integer cardId = 1;
		Integer targetC = 1;
		Integer targetC2 = 2;
		Card card = new Card();
        GenericCard genericCard = new GenericCard();
        genericCard.setId(1);
        genericCard.setType(Type.TRANSPLANT);
        genericCard.setColour(Colour.RED);
		card.setType(genericCard);
		ModelMap model = new ModelMap();
		
		//when
		when(cardService.findCard(cardId)).thenReturn(Optional.of(card));
		
		//then
		assertEquals("playTransplant", gameController.playOnAnotherCard(gameId, cardId, targetC, targetC2, model));
	}

    @Test
		public void playOrgan_validInput_returnsTurn() {
			// Arrange
			int gameId = 1;
			Integer g_id = 2;
			Integer c_id = 3;
			ModelMap model = new ModelMap();
			
			Optional<Card> c = Optional.of(new Card());
			GamePlayer gp1 = new GamePlayer();
			Optional<GamePlayer> gp2 = Optional.of(new GamePlayer());
			
			when(cardService.findCard(c_id)).thenReturn(c);
			when(authenticationService.getGamePlayer()).thenReturn(gp1);
			when(gamePlayerService.findById(g_id)).thenReturn(gp2);
			
			// Act
			String result = gameController.playOrgan(gameId, g_id, c_id, model);
			
			// Assert
			assertEquals("turn", result);
			verify(gameService).addOrgan(c.get(), gp2.get(), gp2.get(), model);
			verify(cardService).save(c.get());
			verify(gamePlayerService).save(gp1);
			verify(gamePlayerService).save(gp2.get());
		}
		
		@Test
		public void playOrgan_invalidInput_returnsMuestraVista() {
			// Arrange
			int gameId = 1;
			Integer g_id = 2;
			Integer c_id = 3;
			ModelMap model = new ModelMap();
			
			Optional<Card> c = Optional.empty();
			GamePlayer gp1 = new GamePlayer();
			Optional<GamePlayer> gp2 = Optional.empty();
			
			when(cardService.findCard(c_id)).thenReturn(c);
			when(authenticationService.getGamePlayer()).thenReturn(gp1);
			when(gamePlayerService.findById(g_id)).thenReturn(gp2);
			
			// Act
			String result = gameController.playOrgan(gameId, g_id, c_id, model);
			
			// Assert
			assertEquals("muestraVista", result);
			verify(model).put("message", "Movimiento inválido");
			verify(model).put("messageType", "info");
		}

        @Test
		public void playVirus_validInput_returnsTurn() {
			// Arrange
			int gameId = 1;
			Integer c1_id = 2;
			Integer c2_id = 3;
			ModelMap model = new ModelMap();
			
			Optional<Card> c1 = Optional.of(new Card());
			Optional<Card> c2 = Optional.of(new Card());
			Card old_card = new Card();
			
			when(cardService.findCard(c1_id)).thenReturn(c1);
			when(cardService.findCard(c2_id)).thenReturn(c2);
			when(c2.get().getVirus().size()).thenReturn(1);
			when(c2.get().getVaccines().size()).thenReturn(1);
			when(c2.get().getVirus().get(0)).thenReturn(old_card);
			when(c2.get().getVaccines().get(0)).thenReturn(old_card);
			
			// Act
			String result = gameController.playVirus(gameId, c1_id, c2_id, model);
			
			// Assert
			assertEquals("turn", result);
			verify(cardService).infect(c2.get(), c1.get());
			verify(cardService).save(c1.get());
			verify(cardService).save(c2.get());
			verify(cardService).save(old_card);
			verify(gameController).turn(gameId);
		}
		
		@Test
		public void playVirus_invalidInput_returnsMuestraVista() {
			// Arrange
			int gameId = 1;
			Integer c1_id = 2;
			Integer c2_id = 3;
			ModelMap model = new ModelMap();
			
			Optional<Card> c1 = Optional.empty();
			Optional<Card> c2 = Optional.empty();
			
			when(cardService.findCard(c1_id)).thenReturn(c1);
			when(cardService.findCard(c2_id)).thenReturn(c2);
			
			// Act
			String result = gameController.playVirus(gameId, c1_id, c2_id, model);
			
			// Assert
			assertEquals("muestraVista", result);
			verify(model).put("message", "Movimiento inválido");
			verify(model).put("messageType", "info");
			verify(gameController).muestraVista(gameId, model);
		}
		
		@Test
		public void playVaccine_validInput_returnsTurn() {
			// Arrange
			int gameId = 1;
			Integer c1_id = 2;
			Integer c2_id = 3;
			ModelMap model = new ModelMap();
			
			Optional<Card> c1 = Optional.of(new Card());
			Optional<Card> c2 = Optional.of(new Card());
			Card old_virus = new Card();
			
			when(cardService.findCard(c1_id)).thenReturn(c1);
			when(cardService.findCard(c2_id)).thenReturn(c2);
			when(c2.get().getVirus().size()).thenReturn(1);
			when(c2.get().getVirus().get(0)).thenReturn(old_virus);
			
			// Act
			String result = gameController.playVaccine(gameId, c1_id, c2_id, model);
			
			// Assert
			assertEquals("turn", result);
			verify(cardService).vaccinate(c2.get(), c1.get());
			verify(cardService).save(c1.get());
			verify(cardService).save(c2.get());
			verify(cardService).save(old_virus);
			verify(gameController).turn(gameId);
		}
		
		@Test
		public void playVaccine_invalidInput_returnsMuestraVista() {
			// Arrange
			int gameId = 1;
			Integer c1_id = 2;
			Integer c2_id = 3;
			ModelMap model = new ModelMap();
			
			Optional<Card> c1 = Optional.empty();
			Optional<Card> c2 = Optional.empty();
			
			when(cardService.findCard(c1_id)).thenReturn(c1);
			when(cardService.findCard(c2_id)).thenReturn(c2);
			
			// Act
			String result = gameController.playVaccine(gameId, c1_id, c2_id, model);
			
			// Assert
			assertEquals("muestraVista", result);
			verify(model).put("message", "Movimiento inválido");
			verify(model).put("messageType", "info");
			verify(gameController).muestraVista(gameId, model);
		}

        @Test
		public void playTransplant_validInput_returnsTurn() {
			// Arrange
			int gameId = 1;
			Integer c1_id = 2;
			Integer c2_id = 3;
			Integer transplantId = 4;
			ModelMap model = new ModelMap();
			
			// Act
			String result = gameController.playTransplant(gameId, c1_id, c2_id, transplantId, model);
			
			// Assert
			assertEquals("turn", result);
		}
		
		@Test
		public void playTransplant_invalidInput_returnsMuestraVista() {
			// Arrange
			int gameId = 1;
			Integer c1_id = 2;
			Integer c2_id = 3;
			Integer transplantId = null;
			ModelMap model = new ModelMap();
			
			// Act
			String result = gameController.playTransplant(gameId, c1_id, c2_id, transplantId, model);
			
			// Assert
			assertEquals("muestraVista", result);
		}

        @Test
		public void playThief_validInput_returnsTurn() {
			// Arrange
			int gameId = 1;
			Integer c_id = 2;
			Integer stolenCardId = 3;
			ModelMap model = new ModelMap();
			
			// Act
			String result = gameController.playThief(gameId, c_id, stolenCardId, model);
			
			// Assert
			assertEquals("turn", result);
		}
		
		@Test
		public void playThief_invalidInput_returnsMuestraVista() {
			// Arrange
			int gameId = 1;
			Integer c_id = null;
			Integer stolenCardId = 3;
			ModelMap model = new ModelMap();
			
			// Act
			String result = gameController.playThief(gameId, c_id, stolenCardId, model);
			
			// Assert
			assertEquals("muestraVista", result);
			assertEquals("Movimiento inválido", model.get("message"));
			assertEquals("info", model.get("messageType"));
		}
		
		@Test
		public void playInfect_validInput_returnsTurn() {
			// Arrange
			int gameId = 1;
			Integer g_id = 2;
			Integer c_id = 3;
			ModelMap model = new ModelMap();
			
			// Act
			String result = gameController.playInfect(gameId, g_id, c_id, model);
			
			// Assert
			assertEquals("turn", result);
		}
		
		@Test
		public void playInfect_invalidInput_returnsMuestraVista() {
			// Arrange
			int gameId = 1;
			Integer g_id = null;
			Integer c_id = 3;
			ModelMap model = new ModelMap();
			
			// Act
			String result = gameController.playInfect(gameId, g_id, c_id, model);
			
			// Assert
			assertEquals("muestraVista", result);
			assertEquals("Jugador no encontrado", model.get("message"));
			assertEquals("info", model.get("messageType"));
		}
		
		@Test
		public void playGlove_validInput_returnsTurn() {
			// Arrange
			int gameId = 1;
			Integer c_id = 2;
			
			// Act
			String result = gameController.playGlove(gameId, c_id);
			
			// Assert
			assertEquals("turn", result);
		}
		
		@Test
		public void playGlove_invalidInput_returnsMuestraVista() {
			// Arrange
			int gameId = 1;
			Integer c_id = null;
			ModelMap model = new ModelMap();
			
			// Act
			String result = gameController.playGlove(gameId, c_id);
			
			// Assert
			assertEquals("muestraVista", result);
			assertEquals("Card not found.", model.get("message"));
			assertEquals("info", model.get("messageType"));
		}

        @Test
	public void testPlayMedicalError() {
		// Arrange
		int gameId = 1;
		Integer g_id = 2;
		Integer c_id = 3;
		ModelMap model = new ModelMap();
		GamePlayer gamePlayer1 = mock(GamePlayer.class);
		GamePlayer gamePlayer2 = mock(GamePlayer.class);
		Card medicalError = mock(Card.class);
		
		when(authenticationService.getGamePlayer()).thenReturn(gamePlayer1);
		when(gamePlayerService.findById(g_id)).thenReturn(Optional.of(gamePlayer2));
		when(cardService.findCard(c_id)).thenReturn(Optional.of(medicalError));
		
		// Act
		String result = gameController.playMedicalError(gameId, g_id, c_id, model);
		
		// Assert
		assertEquals("turn", result);
		verify(gameService).medicalError(gamePlayer1, gamePlayer2);
		verify(medicalError).discard();
		verify(cardService).save(medicalError);
		verify(gamePlayerService).save(gamePlayer1);
		verify(gamePlayerService).save(gamePlayer2);
	}
	
	@Test
	public void testPlayMedicalError_GamePlayerNotFound() {
		// Arrange
		int gameId = 1;
		Integer g_id = 2;
		Integer c_id = 3;
		ModelMap model = new ModelMap();
		GamePlayer gamePlayer1 = mock(GamePlayer.class);
		
		when(authenticationService.getGamePlayer()).thenReturn(gamePlayer1);
		when(gamePlayerService.findById(g_id)).thenReturn(Optional.empty());
		
		// Act
		String result = gameController.playMedicalError(gameId, g_id, c_id, model);
		
		// Assert
		assertEquals("muestraVista", result);
		assertEquals("Jugador no encontrado", model.get("message"));
		assertEquals("info", model.get("messageType"));
		verify(gameService, never()).medicalError(any(), any());
		verify(cardService, never()).save(any());
		verify(gamePlayerService, never()).save(any());
	}
	
	@Test
	public void testPlayMedicalError_CardNotFound() {
		// Arrange
		int gameId = 1;
		Integer g_id = 2;
		Integer c_id = 3;
		ModelMap model = new ModelMap();
		GamePlayer gamePlayer1 = mock(GamePlayer.class);
		GamePlayer gamePlayer2 = mock(GamePlayer.class);
		
		when(authenticationService.getGamePlayer()).thenReturn(gamePlayer1);
		when(gamePlayerService.findById(g_id)).thenReturn(Optional.of(gamePlayer2));
		when(cardService.findCard(c_id)).thenReturn(Optional.empty());
		
		// Act
		String result = gameController.playMedicalError(gameId, g_id, c_id, model);
		
		// Assert
		assertEquals("muestraVista", result);
		assertEquals("Carta no encontrada", model.get("message"));
		assertEquals("info", model.get("messageType"));
		verify(gameService, never()).medicalError(any(), any());
		verify(cardService, never()).save(any());
		verify(gamePlayerService, never()).save(any());
	}

    //Test method for discardView
	@Test
	public void discardViewTest(){
		//Arrange
		Integer gameId = 1;
		ModelMap model = new ModelMap();
		GamePlayer currentGamePlayer = authenticationService.getGamePlayer();
		List<Card> hand = currentGamePlayer.getHand();
		Hand emptyForm = new Hand();
		
		//Act
		String result = gameController.discardView(gameId, model);
		
		//Assert
		assertEquals("games/discard", result);
		assertEquals(hand, model.get("hand"));
		assertEquals(emptyForm, model.get("cardsForm"));
	}
	
	//Test method for discard
	@Test
	public void discardTest(){
		//Arrange
		Integer gameId = 1;
		ModelMap model = new ModelMap();
        Hand cardIds = new Hand();
		GamePlayer currentGamePlayer = authenticationService.getGamePlayer();
		List<Card> cards = cardService.findAllCardsByIds(cardIds.getCards());
		
		//Act
		//String result = gameController.discard(cardIds, gameId, model, br);
		
		//Assert
		//assertEquals("turn(gameId)", result);
		assertTrue(currentGamePlayer.getCards().containsAll(cards));
	}
	
	//Test method for classification
	@Test
	public void classificationTest() throws WonPlayedGamesException{
		//Arrange
		Integer gameId = 1;
		ModelMap model = new ModelMap();
		Game game = new Game();
		GamePlayer gp = new GamePlayer();
		List<GamePlayer> gps = new ArrayList<>();
		game.setWinner(gp);
		gps.add(gp);
		gp.setWinner(true);
		GenericCard generic_heart =new GenericCard(1,Colour.RED, Type.ORGAN);
    	GenericCard generic_stomach =new GenericCard(2,Colour.GREEN, Type.ORGAN);
    	GenericCard generic_rainbow = new GenericCard(3, Colour.RAINBOW, Type.ORGAN);
		GenericCard generic_bone = new GenericCard(12,Colour.YELLOW, Type.ORGAN);
		Card organ_heart = new Card(1, true, gp, generic_heart);
    	Card organ_rainbow = new Card(3, true, gp, generic_rainbow);
    	Card organ_stomach = new Card(2, true, gp, generic_stomach);
		Card organ_bone = new Card(13,true,gp,generic_bone);
		gp.setCards(List.of(organ_heart, organ_rainbow, organ_stomach, organ_bone));
		game.setGamePlayer(gps);
		when(gameService.findGame(gameId)).thenReturn(game);
		//Act
		String result = gameController.classification(gameId, model);
		
		//Assert
		assertEquals("games/classification", result);
		assertEquals(game.getClassification(), model.get("classification"));
		assertTrue(game.hasAnyWinners());
	}
	
}