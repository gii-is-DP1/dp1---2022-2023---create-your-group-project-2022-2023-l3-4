
package org.springframework.samples.petclinic.gamePlayer;


import java.util.*;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.game.Game;
import org.springframework.samples.petclinic.game.GameService;
import org.springframework.samples.petclinic.player.Player;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class GamePlayerService {

	private GamePlayerRepository gamePlayerRepository;
	private GameService gameService;


	@Autowired
	public GamePlayerService(GamePlayerRepository gamePlayerRepository, GameService gameService) {
		this.gamePlayerRepository = gamePlayerRepository;
		this.gameService = gameService;
	}
	@Transactional(readOnly = true)
	public List<GamePlayer> findAll(){
		return gamePlayerRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Optional<GamePlayer> findById(Integer i){
		return gamePlayerRepository.findById(i);
	}

	@Transactional(readOnly = true)
	public GamePlayer getGamePlayerByPlayer(Player player) {
		return gamePlayerRepository.getGamePlayerByPlayer(player);
	}

	@Transactional
	public GamePlayer save(GamePlayer gamePlayer){
		return gamePlayerRepository.save(gamePlayer);
	}

	public GamePlayerService() {}

	public GamePlayer saveGamePlayerForNewPlayer(@Valid Player player) {
		GamePlayer gamePlayer = new GamePlayer();
		gamePlayer.setPlayer(player);
		return save(gamePlayer);
	}

	@Transactional
	public void deleteGamePlayer(GamePlayer gamePlayer) {
		List<Game> games = gameService.findGamesByGameplayer(gamePlayer);
		for (Game g: games) {
			if (g != null) {
				g.getGamePlayer().remove(gamePlayer);
				if (g.getWinner().equals(gamePlayer)) {
					g.setWinner(null);
				}
			}
		}
		gamePlayerRepository.delete(gamePlayer);
	}
	
}
