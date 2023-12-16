package com.tezov.medium.adr.transition_animation.notifier

interface Event{

    object Close : Event
    object Cancel : Event
    object Confirm : Event
    object OwnerShipLost : Event

}