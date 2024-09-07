package dev.jxmen.cs.ai.interviewer.common

import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.persistence.entity.subject.JpaSubject
import dev.jxmen.cs.ai.interviewer.persistence.port.output.SubjectCommandRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DatabaseInitializer(
    private val subjectCommandRepository: SubjectCommandRepository,
) {
    private val logger = LoggerFactory.getLogger(DatabaseInitializer::class.java)

    fun initData() {
        logger.info("Initializing data")
        subjectCommandRepository.saveAll(getSubjects())
        logger.info("Data initialized")
    }

    private fun getSubjects(): List<JpaSubject> {
        val dsaJpaSubjects =
            listOf(
                JpaSubject(
                    title = "시간 복잡도와 공간 복잡도",
                    question = "시간 복잡도와 공간 복잡도에 대해 설명하고 왜 중요한지 말씀해 주세요.",
                    category = SubjectCategory.DSA,
                ),
                JpaSubject(
                    title = "퀵 정렬의 작동 원리",
                    question = "퀵 정렬 알고리즘의 작동 원리와 평균/최악의 시간 복잡도를 설명해주세요.",
                    category = SubjectCategory.DSA,
                ),
                JpaSubject(
                    title = "해시 충돌 해결 방법",
                    question = "해시 테이블에서 충돌이 발생했을 때 해결할 수 있는 방법들을 설명해주세요.",
                    category = SubjectCategory.DSA,
                ),
                JpaSubject(
                    title = "DFS와 BFS의 차이",
                    question = "깊이 우선 탐색(DFS)와 너비 우선 탐색(BFS)의 차이점과 각각 어떤 상황에서 유용한지 설명해주세요.",
                    category = SubjectCategory.DSA,
                ),
                JpaSubject(
                    title = "동적 프로그래밍이란?",
                    question = "동적 프로그래밍이 무엇이며 어떤 문제를 해결하는 데 사용되는지 예를 들어 설명해주세요.",
                    category = SubjectCategory.DSA,
                ),
                JpaSubject(
                    title = "이진 탐색 트리의 특징",
                    question = "이진 탐색 트리의 특징과 장단점을 설명해주세요. 균형 잡힌 이진 탐색 트리의 예시도 들어주세요.",
                    category = SubjectCategory.DSA,
                ),
                JpaSubject(
                    title = "스택과 큐의 차이점",
                    question = "스택과 큐의 차이점을 설명하고 각각의 실제 응용 사례를 들어주세요.",
                    category = SubjectCategory.DSA,
                ),
                JpaSubject(
                    title = "최소 신장 트리 ",
                    question = "최소 신장 트리가 무엇인지 설명하고 이를 구하는 대표적인 알고리즘을 소개해주세요.",
                    category = SubjectCategory.DSA,
                ),
                JpaSubject(
                    title = "그래프 표현 방식",
                    question = "그래프를 컴퓨터에서 표현하는 방식들을 설명하고 각 방식의 장단점을 비교해주세요.",
                    category = SubjectCategory.DSA,
                ),
                JpaSubject(
                    title = " 정렬 알고리즘 비교 ",
                    question = "버블 정렬, 선택 정렬, 삽입 정렬, 병합 정렬의 시간 복잡도를 비교하고, \"최선, 평균, 최악\" 경우를 각각 설명해주세요.",
                    category = SubjectCategory.DSA,
                ),
            )

        return listOf(dsaJpaSubjects).flatten()
    }
}
