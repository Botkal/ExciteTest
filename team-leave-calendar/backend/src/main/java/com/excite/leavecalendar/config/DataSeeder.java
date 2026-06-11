package com.excite.leavecalendar.config;

import com.excite.leavecalendar.entity.TeamMember;
import com.excite.leavecalendar.repository.TeamMemberRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements ApplicationRunner {

    private final TeamMemberRepository teamMemberRepository;

    public DataSeeder(TeamMemberRepository teamMemberRepository) {
        this.teamMemberRepository = teamMemberRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (teamMemberRepository.count() == 0) {
            List<TeamMember> members = List.of(
                    new TeamMember("Alice"),
                    new TeamMember("Bob"),
                    new TeamMember("Charlie"),
                    new TeamMember("Diana")
            );
            teamMemberRepository.saveAll(members);
        }
    }
}
