(ns messenger.components.contact-list.contact-list
  (:require-macros
   [natal-shell.components :refer [view text image touchable-highlight list-view
                                   toolbar-android]]
   [natal-shell.core :refer [with-error-view]])
  (:require [om.next :as om :refer-macros [defui]]
            [messenger.state :as state]
            [messenger.utils.utils :refer [log toast http-post]]
            [messenger.utils.resources :as res]
            [messenger.comm.intercom :as intercom]
            [messenger.components.contact-list.contact :refer [contact]]))

(defn render-row [nav row section-id row-id]
  (contact (om/computed (js->clj row :keywordize-keys true)
                        {:nav nav})))

(defn load-contacts []
  (intercom/load-syng-contacts))

(defui ContactList
  static om/IQuery
  (query [this]
         '[:contacts-ds])
  Object
  (componentDidMount [this]
                     (load-contacts))
  (render [this]
          (let [{:keys [contacts-ds]} (om/props this)
                {:keys [nav]} (om/get-computed this)]
            (view {:style {:flex 1
                           :backgroundColor "white"}}
                  (toolbar-android {:logo res/logo-icon
                                    :title "Chats"
                                    :titleColor "#4A5258"
                                    :style {:backgroundColor "white"
                                            :height 56
                                            :elevation 2}})
                  (list-view {:dataSource contacts-ds
                              :renderRow (partial render-row nav)
                              :style {:backgroundColor "white"}})))))

(def contact-list (om/factory ContactList))
