(ns syng-im.components.contact-list.contact-list
  (:require-macros
   [natal-shell.data-source :refer [data-source clone-with-rows]]
   [natal-shell.core :refer [with-error-view]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [view text image touchable-highlight
                                              navigator list-view toolbar-android
                                              list-item]]
            [syng-im.components.resources :as res]
            [syng-im.components.contact-list.contact :refer [contact-view]]
            [syng-im.utils.logging :as log]
            ;; [messenger.comm.intercom :as intercom]
            ;; [messenger.components.contact-list.contact :refer [contact]]
            ;; [messenger.components.iname :as in]
            ))

(defn render-row [row section-id row-id]
  (list-item (contact-view (js->clj row :keywordize-keys true))))

;; (defn load-contacts []
;;   (intercom/load-syng-contacts))

;; (defui ContactList
;;   static in/IName
;;   (get-name [this]
;;     :contacts/contacts)
;;   static om/IQuery
;;   (query [this]
;;     '[:contacts-ds])
;;   Object
;;   (componentDidMount [this]
;;     (load-contacts))
;;   (render [this]
;;     (let [{{contacts-ds :contacts-ds} :contacts/contacts} (om/props this)
;;           {:keys [nav]} (om/get-computed this)]
;;       (view {:style {:flex            1
;;                      :backgroundColor "white"}}
;;             (toolbar-android {:logo       res/logo-icon
;;                               :title      "Chats"
;;                               :titleColor "#4A5258"
;;                               :style      {:backgroundColor "white"
;;                                            :height          56
;;                                            :elevation       2}})
;;             (when contacts-ds
;;               (list-view {:dataSource contacts-ds
 ;;                           :renderRow  (partial render-row nav)
;;                           :style      {:backgroundColor "white"}}))))))

;; (def contact-list (om/factory ContactList))

(defn get-data-source [contacts]
  (clone-with-rows (data-source {:rowHasChanged (fn [row1 row2]
                                                  (not= row1 row2))})
                   contacts))

(defn contacts-list-re-frame [contacts]
  (let [contacts-ds (get-data-source contacts)]
    [view {:style {:flex            1
                   :backgroundColor "white"}}
     [toolbar-android {:logo       res/logo-icon
                       :title      "Chats"
                       :titleColor "#4A5258"
                       :style      {:backgroundColor "white"
                                    :height          56
                                    :elevation       2}}]
     (when contacts-ds
       [list-view {:dataSource contacts-ds
                   :renderRow   render-row
                   :style      {:backgroundColor "white"}}]
       )
     ]))





(def logo-img (js/require "./images/cljs.png"))


(defn alert [title]
  (.alert (.-Alert js/React) title))

(defn contact-list [{:keys [navigator]}]
  (let [greeting (subscribe [:get-greeting])
        contacts (subscribe [:get-contacts])]
    (fn []
      (contacts-list-re-frame @contacts)
      
      ;; [view {:style {:flex-direction "column" :margin 40 :align-items "center"}}
      ;;  [text {:style {:font-size 30 :font-weight "100" :margin-bottom 20 :text-align "center"}} (str @greeting " " (count @contacts))]
      ;;  [image {:source logo-img
      ;;          :style  {:width 80 :height 80 :margin-bottom 30}}]
      ;;  [touchable-highlight {:style    {:background-color "#999" :padding 10 :border-radius 5}
      ;;                        :on-press #(alert "HELLO!")}
      ;;   [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "press me"]]]
      )))
