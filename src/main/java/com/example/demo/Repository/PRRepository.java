//package com.example.demo.Repository;
//
//import com.example.demo.models.PurchaseRequest;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//@Repository
//public interface PRRepository extends JpaRepository<PurchaseRequest, Long> {
//}
package com.example.demo.Repository;

import com.example.demo.models.POStatus;
import com.example.demo.models.PurchaseRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PRRepository extends JpaRepository<PurchaseRequest, Long> {
    // This interface allows POService to use .findById(prId)
    long countByStatus(String status);

    List<PurchaseRequest> findByCreatedByUsername(String username);

}

