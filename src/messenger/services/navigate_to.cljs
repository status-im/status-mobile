(ns messenger.services.navigate-to
  (:require [syng-im.utils.logging :as log]
            [messenger.state :as state]
            [messenger.components.chat.chat :as chat]
            [messenger.android.sign-up-confirm :as sc]
            [messenger.components.contact-list.contact-list :as cl]
            [messenger.models.chat :refer [set-current-chat-id]]
            [om.next :as om]
            [messenger.omnext :as omnext]
            [messenger.components.iname :as in]
            [messenger.models.navigation :as n]))

(defn nav-push [nav route]
  (binding [state/*nav-render* false]
    (.push nav (clj->js route))))

(defn nav-replace [nav route]
  (binding [state/*nav-render* false]
    (.replace nav (clj->js route))))

(defmulti navigate-to (fn [state id args]
                        id))

(defmethod navigate-to :scene/chat
  [state id {:keys [navigator chat-id] :as args}]
  (log/debug "handling " id "args = " (dissoc args :navigator))
  (n/set-current-screen-class chat/Chat)
  (set-current-chat-id chat-id)
  (nav-push navigator {:component chat/chat}))

(defmethod navigate-to :scene/signup-confirm
  [state id {:keys [navigator] :as args}]
  (log/debug "handling " id "args = " (dissoc args :navigator))
  (n/set-current-screen-class sc/SignUpConfirm)
  (nav-replace navigator {:component sc/sign-up-confirm
                          :name      "sign-up-confirm"}))

(defmethod navigate-to :scene/contacts
  [state id {:keys [navigator] :as args}]
  (log/debug "handling " id "args = " (dissoc args :navigator))
  (n/set-current-screen-class cl/ContactList)
  (nav-replace navigator {:component cl/contact-list
                          :name      "contact-list"}))

(defn navigate-to-handler [state [id args]]
  (log/debug "navigate-to-handler: " (dissoc args :navigator))
  (navigate-to state id args))


(comment

  )