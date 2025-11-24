package com.babu.appp.json


import org.json.JSONObject

data class CourseStructure(
    val branches: List<String>,
    val semesters: List<String>,
    val subjects: Map<String, Map<String, List<String>>>,
    val years: List<String>
) {
    companion object {
        fun fromJson(json: JSONObject): CourseStructure {
            val branches = json.getJSONArray("BRANCHES").let { arr -> List(arr.length()) { arr.getString(it) } }
            val semesters = json.getJSONArray("SEMESTERS").let { arr -> List(arr.length()) { arr.getString(it) } }
            val years = json.getJSONArray("YEARS").let { arr -> List(arr.length()) { arr.getString(it) } }

            val subjects = mutableMapOf<String, Map<String, List<String>>>()
            val subjectsJson = json.getJSONObject("SUBJECTS")
            for (branchKey in subjectsJson.keys()) {
                val semMap = mutableMapOf<String, List<String>>()
                val branchObj = subjectsJson.getJSONObject(branchKey)
                for (semKey in branchObj.keys()) {
                    val subjectList = branchObj.getJSONArray(semKey).let { arr -> List(arr.length()) { arr.getString(it) } }
                    semMap[semKey] = subjectList
                }
                subjects[branchKey] = semMap
            }

            return CourseStructure(branches, semesters, subjects, years)
        }
    }
}
