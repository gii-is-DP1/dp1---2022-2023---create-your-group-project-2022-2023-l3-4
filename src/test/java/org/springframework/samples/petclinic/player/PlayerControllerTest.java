package org.springframework.samples.petclinic.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.samples.petclinic.configuration.SecurityConfiguration;
import org.springframework.samples.petclinic.friendRequest.FriendService;
import org.springframework.samples.petclinic.game.Game;
import org.springframework.samples.petclinic.game.GameService;
import org.springframework.samples.petclinic.gamePlayer.GamePlayer;
import org.springframework.samples.petclinic.util.AuthenticationService;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@WebMvcTest(controllers=PlayerController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class),
    excludeAutoConfiguration = SecurityConfiguration.class)
public class PlayerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private GameService gameService;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private FriendService friendService;

    @BeforeEach
    public void setUp() {
        Player mockPlayer = new Player();
        GamePlayer mockGamePlayer = new GamePlayer();
        Page<Game> mockGames = new PageImpl<>(new ArrayList<>());
        when(authenticationService.getPlayer()).thenReturn(mockPlayer);
        when(authenticationService.getGamePlayer()).thenReturn(mockGamePlayer);
        when(gameService.findGamesByGameplayerPaged(eq(mockGamePlayer), any())).thenReturn(mockGames);
        when(gameService.humanReadableDuration(any())).thenReturn("string");
        when(gameService.getNumGamesPlayed(mockGamePlayer)).thenReturn(10);
        when(gameService.getNumGamesWon(mockGamePlayer)).thenReturn(5);
    }
    
    @WithMockUser
    @Test
    public void testPlayerProfile() throws Exception {
        mockMvc.perform(get("/player/me"))
            .andExpect(status().isOk())
            .andExpect(view().name("player/playerProfile"))
            .andExpect(model().attributeExists("player"))
            .andExpect(model().attributeExists("gameplayer"))
            .andExpect(model().attributeExists("games"))
            .andExpect(model().attributeExists("currentPage"))
            .andExpect(model().attributeExists("totalPages"))
            .andExpect(model().attributeExists("statistics"))
            .andExpect(model().attributeExists("totalTimePlayed"));
    }

    @WithMockUser
    @Test
    public void testPlayerProfileEdit() throws Exception {
        mockMvc.perform(get("/player/me/edit"))
            .andExpect(status().isOk())
            .andExpect(view().name("player/createOrUpdateProfileForm"))
            .andExpect(model().attributeExists("player"));
    }

    @WithMockUser
    @Test
    public void testPlayerProfileEditPost() throws Exception {
        mockMvc.perform(post("/player/me/edit")
        .with(csrf())
        .param("firstName", "Pedro")
        .param("lastName", "Lopez")
        .param("status", "true")
        .param("description", "nueva descripcion."))
        .andExpect(status().isOk())
        .andExpect(view().name("player/playerProfile"))
        .andExpect(model().attributeExists("message"));
    }

    @WithMockUser
    @Test
    public void testPlayerProfileEditEmptyFirstName() throws Exception {
        mockMvc.perform(post("/player/me/edit")
        .with(csrf())
        .param("firstName", "")
        .param("lastName", "Lopez")
        .param("status", "true")
        .param("description", "nueva descripcion."))
        .andExpect(status().isOk())
        .andExpect(view().name("player/createOrUpdateProfileForm"))
        .andExpect(model().hasErrors());
    }

    @WithMockUser
    @Test
    public void testPlayerProfileEditEmptyLastName() throws Exception {
        mockMvc.perform(post("/player/me/edit")
        .with(csrf())
        .param("firstName", "Emmanuel")
        .param("lastName", "")
        .param("status", "true")
        .param("description", "nueva descripcion."))
        .andExpect(status().isOk())
        .andExpect(view().name("player/createOrUpdateProfileForm"))
        .andExpect(model().hasErrors());
    }

    @WithMockUser
    @Test
    public void testSearch() throws Exception {
        mockMvc.perform(get("/player/search"))
        .andExpect(status().isOk())
        .andExpect(view().name("player/searchPlayer"))
        .andExpect(model().attributeExists("player"));
    }

    @WithMockUser
    @Test
    public void testSearchPost() throws Exception {
        Player mockPlayer = new Player();
        Player anotherPlayer = new Player();
        List<Player> players = new ArrayList<>();
        players.add(mockPlayer);
        players.add(anotherPlayer);

        when(playerService.getPlayersByUsername("frabenrui1")).thenReturn(players);

        mockMvc.perform(post("/player/search")
        .with(csrf())
        .param("user.username", "frabenrui1"))
        .andExpect(status().isOk())
        .andExpect(view().name("player/playerListing"))
        .andExpect(model().attributeExists("players"))
        .andExpect(model().attribute("players", players));
    }

    @WithMockUser
    @Test
    public void testSearchNullUsername() throws Exception {
        when(playerService.getPlayersByUsername("")).thenReturn(new ArrayList<>());

        mockMvc.perform(post("/player/search")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("player/searchPlayer"))
        .andExpect(model().hasErrors());
    }
}
