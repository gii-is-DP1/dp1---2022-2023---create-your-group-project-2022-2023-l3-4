package org.springframework.samples.virus.room;

import java.util.Collection;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.virus.player.Player;
import org.springframework.samples.virus.player.PlayerService;
import org.springframework.samples.virus.room.exceptions.DuplicatedNameRoomException;
import org.springframework.samples.virus.room.exceptions.PlayerHostsExistingRoomException;
import org.springframework.samples.virus.util.AuthenticationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/room")
public class RoomController {

    private static final String VIEWS_ROOM_CREATE_OR_UPDATE_FORM = "rooms/createOrUpdateRoomForm";
	private static final String VIEWS_ROOM_SEARCH = "rooms/createOrSearch";
	private static final String VIEWS_WAITING_ROOM = "rooms/waitingRoom";

    
	private final RoomService roomService;



	@Autowired
    private AuthenticationService authService;

	@Autowired
    private PlayerService playerService;

    @Autowired
	public RoomController(RoomService roomService) {
		this.roomService = roomService;        
	}

	@GetMapping(value = "/new")
	public String initCreationForm(Player player, ModelMap model) {
		Room room = new Room();
		
		model.put("room", room);
		model.put("player", player);
		return VIEWS_ROOM_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping(value = "/new")
	public String processCreationForm(@Valid Room room, BindingResult result, ModelMap model) {	
		Player player = authService.getPlayer();
		if (result.hasErrors()) {
			model.put("room", room);
			return VIEWS_ROOM_CREATE_OR_UPDATE_FORM;
		}
		else {
				try{
					room.setTotalGamesPlayer(0);
					room.setHost(player);
					this.roomService.saveRoom(room);

				}catch(DuplicatedNameRoomException | DataAccessException | PlayerHostsExistingRoomException ex){
					if(ex.getClass().equals(DuplicatedNameRoomException.class)){
						result.rejectValue("roomName", "duplicate", "already exists");
					} else {
						model.put("message", "You are already host of another room.");
						model.put("messageType", "warning");
					}
					return VIEWS_ROOM_CREATE_OR_UPDATE_FORM;
				}
				return "redirect:/room/" + room.getId();
		}
	}

	@GetMapping("/createSearch")
	public String createSearch(ModelMap model) {
		Player player = authService.getPlayer();
		model.put("player", player);
		model.put("room", new Room());
		return VIEWS_ROOM_SEARCH;
	}

	@GetMapping(value = "/find")
	public String processFindRoomForm(Room room, BindingResult result, ModelMap model) {
		// allow parameterless GET request for /find to return all records
		if (room.getRoomName() == null) {
			room.setRoomName(""); // empty string signifies broadest possible search
		}

		// find owners by last name
		Collection<Room> results = roomService.findRoomsByRoomName(room.getRoomName());
		if (results.isEmpty()) {
			// no rooms found
			result.rejectValue("roomName", "notFound", "not found");
			return "rooms/createOrSearch";
		}

		else {
			// multiple roomss found
			model.put("rooms", results);
			return "rooms/roomsList";
		}
	}

	@GetMapping("/{roomId}")
	public String showRoom(@PathVariable("roomId") int roomId,ModelMap model) {
		Player player = authService.getPlayer();
		Room room=this.roomService.findRoomById(roomId);
		player.setRoom(room);
		this.playerService.savePlayer(player);
		Collection<Player> players = room.getPlayers();
		model.put("room", this.roomService.findRoomById(roomId));
		model.put("players",players);
		model.put("countPlayer",players.size());
		model.put("host", player.equals(room.getHost()));
		
		return VIEWS_WAITING_ROOM;
	}

	@GetMapping("/delete/{roomId}")
	public String deleteRoom(@PathVariable("roomId") int roomId, ModelMap model){
		Player player = authService.getPlayer();
		Room room =this.roomService.findRoomById(roomId);
		if(room.getHost().equals(player)){
			model.remove("room",this.roomService.findRoomById(roomId));
			player.setRoom(null);
			playerService.savePlayer(player);
			roomService.deleteRoom(roomId);
			return "redirect:/room/find";
		} else {
			return "redirect:/";
		}


	}

}