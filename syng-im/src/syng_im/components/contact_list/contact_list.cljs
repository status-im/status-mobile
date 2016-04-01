(ns syng-im.components.contact-list.contact-list
  (:require-macros
   [natal-shell.data-source :refer [data-source clone-with-rows]]
   [natal-shell.core :refer [with-error-view]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [view text image touchable-highlight
                                              navigator list-view toolbar-android
                                              list-item]]
            [syng-im.components.contact-list.contact :refer [contact-view]]
            [syng-im.resources :as res]
            [syng-im.utils.logging :as log]))

(defn render-row [navigator row section-id row-id]
  (list-item [contact-view {:navigator navigator
                            :contact (js->clj row :keywordize-keys true)}]))

(defn get-data-source [contacts]
  (clone-with-rows (data-source {:rowHasChanged (fn [row1 row2]
                                                  (not= row1 row2))})
                   contacts))

(defn contact-list [{:keys [navigator]}]
  (let [contacts (subscribe [:get-contacts])]
    (fn []
      (let [contacts-ds (get-data-source @contacts)]
        [view {:style {:flex            1
                       :backgroundColor "white"}}
         [toolbar-android {:logo       res/logo-icon
                           :title      "Contacts"
                           :titleColor "#4A5258"
                           :style      {:backgroundColor "white"
                                        :height          56
                                        :elevation       2}}]
         (when contacts-ds
           [list-view {:dataSource contacts-ds
                       :renderRow  (partial render-row navigator)
                       :style      {:backgroundColor "white"}}])]))))
