package com.talha.quiz.projectsem2.service;

import com.talha.quiz.projectsem2.model.Question;
import com.talha.quiz.projectsem2.model.Result;

import java.util.List;

public class QuizService {

    public static List<Question> loadQuestions() {
        return List.of(
            new Question(
                "Two graphs are isomorphic if they have the same:",
                "A. Number of vertices only",
                "B. Number of edges only",
                "C. Degree sequence and structural correspondence",
                "D. Same vertex labels",
                "C"
            ),
            new Question(
                "What is the degree of a vertex in a graph?",
                "A. The number of edges in the graph",
                "B. The number of edges incident to that vertex",
                "C. The distance to the farthest vertex",
                "D. The number of vertices in the graph",
                "B"
            ),
            new Question(
                "A complete graph K5 has how many edges?",
                "A. 5",
                "B. 8",
                "C. 10",
                "D. 20",
                "C"
            ),
            new Question(
                "Which is a necessary condition for two graphs to be isomorphic?",
                "A. Same vertex labels",
                "B. Same degree sequence",
                "C. Same number of connected components",
                "D. Both B and C",
                "D"
            ),
            new Question(
                "A graph is called bipartite if:",
                "A. It has exactly two vertices",
                "B. Its vertices can be split into two sets with edges only between sets",
                "C. Every vertex has degree 2",
                "D. It has no cycles",
                "B"
            ),
            new Question(
                "An Euler circuit exists if and only if:",
                "A. The graph is connected and every vertex has even degree",
                "B. The graph has no cycles",
                "C. Every vertex has odd degree",
                "D. The graph is a tree",
                "A"
            ),
            new Question(
                "A tree with n vertices has how many edges?",
                "A. n",
                "B. n + 1",
                "C. n - 1",
                "D. n squared",
                "C"
            ),
            new Question(
                "The VF2 algorithm for graph isomorphism uses:",
                "A. Breadth-first search",
                "B. Backtracking with state-space exploration",
                "C. Dijkstra's algorithm",
                "D. Dynamic programming",
                "B"
            ),
            new Question(
                "Euler's formula for a connected planar graph is V - E + F =",
                "A. 0",
                "B. 1",
                "C. 2",
                "D. V",
                "C"
            ),
            new Question(
                "A Hamiltonian path is a path that:",
                "A. Visits every edge exactly once",
                "B. Visits every vertex exactly once",
                "C. Is the shortest path between two vertices",
                "D. Has no repeated edges",
                "B"
            )
        );
    }

    public static void saveResult(Result result) {
        // No quiz results table in schema — persistence skipped
    }
}
