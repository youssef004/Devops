package tn.esprit.spring.services;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tn.esprit.spring.entities.Departement;
import tn.esprit.spring.entities.Employe;
import tn.esprit.spring.entities.Mission;
import tn.esprit.spring.entities.Role;
import tn.esprit.spring.entities.Timesheet;
import tn.esprit.spring.entities.TimesheetPK;
import tn.esprit.spring.repository.DepartementRepository;
import tn.esprit.spring.repository.EmployeRepository;
import tn.esprit.spring.repository.MissionRepository;
import tn.esprit.spring.repository.TimesheetRepository;
import org.apache.log4j.Logger;
@Service
public class TimesheetServiceImpl implements ITimesheetService {
	private static final Logger l = Logger.getLogger(TimesheetServiceImpl.class);

	@Autowired
	MissionRepository missionRepository;
	@Autowired
	DepartementRepository deptRepoistory;
	@Autowired
	TimesheetRepository timesheetRepository;
	@Autowired
	EmployeRepository employeRepository;
	
	public int ajouterMission(Mission mission) {
		missionRepository.save(mission);
		l.info("mission ajouter");
		return mission.getId();
	}
    
	public void affecterMissionADepartement(int missionId, int depId) {
		Optional<Mission> val1 = missionRepository.findById(missionId);
		Optional<Departement> val2 = deptRepoistory.findById(depId);
		if(val1.isPresent() && val2.isPresent()) {

			Mission mission=val1.get();
			Departement dep=val2.get();

			mission.setDepartement(dep);
			l.info("la mission est affectée au departement");
			missionRepository.save(mission);
		}
		
		
	}

	public void ajouterTimesheet(int missionId, int employeId, Date dateDebut, Date dateFin) {
		TimesheetPK timesheetPK = new TimesheetPK();
		timesheetPK.setDateDebut(dateDebut);
		timesheetPK.setDateFin(dateFin);
		timesheetPK.setIdEmploye(employeId);
		timesheetPK.setIdMission(missionId);
		
		Timesheet timesheet = new Timesheet();
		timesheet.setTimesheetPK(timesheetPK);
		timesheet.setValide(false); //par defaut non valide
		l.info("Timesheet ajouter");
		timesheetRepository.save(timesheet);
		
	}

	
	public void validerTimesheet(int missionId, int employeId, Date dateDebut, Date dateFin, int validateurId) {
		System.out.println("In valider Timesheet");
		Optional<Employe> val1 = employeRepository.findById(validateurId);
		Optional<Mission> val2 = missionRepository.findById(missionId);
		if(val1.isPresent() && val2.isPresent()) {
			Employe validateur=val1.get();
			Mission mission=val2.get();
			if (!validateur.getRole().equals(Role.CHEF_DEPARTEMENT)) {
				//System.out.println("l'employe doit etre chef de departement pour valider une feuille de temps !");
				l.info("l'employe doit etre chef de departement pour valider une feuille de temps !");
				return;
			}
			//verifier s'il est le chef de departement de la mission en question
			boolean chefDeLaMission = false;
			for (Departement dep : validateur.getDepartements()) {
				if (dep.getId() == mission.getDepartement().getId()) {
					chefDeLaMission = true;
					break;
				}
			}
			if (!chefDeLaMission) {
				//System.out.println("l'employe doit etre chef de departement de la mission en question");
				l.info("l'employe doit etre chef de departement de la mission en question");
				return;
			}
//
			TimesheetPK timesheetPK = new TimesheetPK(missionId, employeId, dateDebut, dateFin);
			Timesheet timesheet = timesheetRepository.findBytimesheetPK(timesheetPK);
			timesheet.setValide(true);
			//Comment Lire une date de la base de données
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

			System.out.println("dateDebut : " + dateFormat.format(timesheet.getTimesheetPK().getDateDebut()));
			l.info("dateDebut : " + dateFormat.format(timesheet.getTimesheetPK().getDateDebut()));
		}
	}

	
	public List<Mission> findAllMissionByEmployeJPQL(int employeId) {
		l.info("afficher liste de employer");
		return timesheetRepository.findAllMissionByEmployeJPQL(employeId);
	}

	
	public List<Employe> getAllEmployeByMission(int missionId) {
		l.info("afficher tous les employées par mission");
		return timesheetRepository.getAllEmployeByMission(missionId);
	}

}
