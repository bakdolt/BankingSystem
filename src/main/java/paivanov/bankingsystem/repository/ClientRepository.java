package paivanov.bankingsystem.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import paivanov.bankingsystem.model.Client;

@Repository
public interface ClientRepository extends CrudRepository<Client, Integer> {
}
