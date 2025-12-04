package com.web.back.repositories;

import com.web.back.model.entities.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Integer> {
    @Query(
            value = "SELECT * FROM evaluation WHERE CAST(fecha AS DATE) BETWEEN :beginDate AND :endDate AND num_empleado = :numEmpleado",
            nativeQuery = true
    )
    List<Evaluation> findByFechaAndEmpleado(@Param("numEmpleado") String numEmpleado, @Param("beginDate") String beginDate, @Param("endDate") String endDate);

    @Query(
            value = "SELECT * FROM evaluation WHERE fecha LIKE :fecha% AND horario = :horario AND (:turn IS NULL OR turn = :turn) AND sociedad = :sociedad AND area_nomina = :areaNomina AND num_empleado = :numEmpleado LIMIT 1",
            nativeQuery = true
    )
    Optional<Evaluation> findByFechaAndHorarioAndTurnAndAreaNominaAndSociedadAndEmpleado(@Param("fecha") String fecha, @Param("horario") String horario, @Param("turn") Integer turn, @Param("sociedad") String sociedad, @Param("areaNomina") String areaNomina, @Param("numEmpleado") String numEmpleado);


    @Query(
            value = "SELECT * FROM evaluation WHERE CAST(fecha AS DATE) BETWEEN :beginDate AND :endDate AND sociedad = :sociedad AND area_nomina = :areaNomina order by fecha",
            nativeQuery = true
    )
    List<Evaluation> findByFechaAndAreaNominaAndSociedad(@Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("sociedad") String sociedad, @Param("areaNomina") String areaNomina);

    @Query(
            value = "SELECT * FROM evaluation WHERE CAST(fecha AS DATE) BETWEEN :beginDate AND :endDate AND (:turn IS NULL OR turn = :turn) order by fecha",
            nativeQuery = true
    )
    List<Evaluation> findByFechaAndTurn(@Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("turn") Integer turn);

    @Query(
            value = "SELECT * FROM evaluation WHERE num_empleado in :employee_numbers order by fecha",
            nativeQuery = true
    )
    List<Evaluation> findAllByEmployeeNumber(@Param("employee_numbers") List<String> employeeNumbers);

    @Query(
            value = "SELECT * FROM evaluation " +
                    "WHERE CAST(fecha AS DATE) = (" +
                    "  SELECT MAX(CAST(e2.fecha AS DATE)) " +
                    "  FROM evaluation e2 " +
                    "  WHERE CAST(e2.fecha AS DATE) BETWEEN :beginDate AND :endDate " +
                    "    AND e2.area_nomina = :areaNomina " +
                    "    AND e2.sociedad = :sociedad" +
                    ") " +
                    "AND CAST(fecha AS DATE) BETWEEN :beginDate AND :endDate " +
                    "AND area_nomina = :areaNomina " +
                    "AND sociedad = :sociedad",
            nativeQuery = true
    )
    List<Evaluation> findLatestEvaluationsByAreaNominaAndSociedadAndFechaBetween(
            @Param("areaNomina") String areaNomina,
            @Param("sociedad") String sociedad,
            @Param("beginDate") String beginDate,
            @Param("endDate") String endDate
    );
}
