package team.doit.do_it.listeners

interface OnBindViewHolderListener<H, T> {

    fun onBindViewHolderSucceed(holder: H, item : T,)
}