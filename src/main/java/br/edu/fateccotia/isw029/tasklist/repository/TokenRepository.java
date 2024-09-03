package br.edu.fateccotia.isw029.tasklist.repository;

import org.springframework.data.repository.CrudRepository;

import br.edu.fateccotia.isw029.tasklist.model.Token;

public interface TokenRepository extends CrudRepository<Token, Integer>{
	
}
