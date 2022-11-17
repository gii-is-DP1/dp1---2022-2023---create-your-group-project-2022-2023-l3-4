package org.springframework.samples.petclinic.room;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;



@DataJpaTest
public class RoomRepositoryTest {
    @Autowired
    RoomRepository roomRepository;

    @Test
    public void testFindRoomsByRoomName(){
        Collection<Room> rooms = roomRepository.findRoomsByRoomName("sala");
        assertNotNull(rooms, "El repositorio ha devuelto una lista nula");
        assertEquals(2,rooms.size(),"Faltan datos de inicialización");
    }

    @Test
    public void testFindByRoomName(){
        Room room = roomRepository.findByRoomName("sala1");
        assertNotNull(room, "El repositorio ha devuelto una lista nula");
        assertEquals("sala1", room.roomName, "El nombre de la sala buscada no coincide con la devuelta del repositrio");
    }

}