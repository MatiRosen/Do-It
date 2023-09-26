package team.doit.do_it.listeners

import team.doit.do_it.entities.ProjectEntity

interface OnViewItemClickedListener {
    fun onViewItemDetail(project: ProjectEntity)
}