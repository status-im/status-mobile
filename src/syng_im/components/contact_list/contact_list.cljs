(ns syng-im.components.contact-list.contact-list
  (:require-macros
   [natal-shell.data-source :refer [data-source clone-with-rows]]
   [natal-shell.core :refer [with-error-view]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [view text image touchable-highlight
                                              navigator list-view
                                              list-item]]
            [syng-im.components.contact-list.contact :refer [contact-view]]
            [syng-im.components.styles :refer [font
                                               title-font
                                               color-white
                                               color-black
                                               color-blue
                                               text1-color
                                               text2-color
                                               toolbar-background2]]
            [syng-im.components.toolbar :refer [toolbar]]
            [syng-im.navigation :refer [nav-pop]]
            [syng-im.resources :as res]
            [syng-im.utils.logging :as log]))

(defn render-row [navigator row section-id row-id]
  (list-item [contact-view {:navigator navigator
                            :contact (js->clj row :keywordize-keys true)}]))

(defn get-data-source [contacts]
  (clone-with-rows (data-source {:rowHasChanged (fn [row1 row2]
                                                  (not= row1 row2))})
                   contacts))

(defn contact-list-toolbar [navigator]
  [toolbar {:navigator        navigator
            :title            "Contacts"
            :background-color toolbar-background2
            :action           {:image {:source {:uri "icon_search"}
                                       :style  {:width  17
                                                :height 17}}
                               :handler (fn [])}}])

(defn contact-list [{:keys [navigator]}]
  (let [contacts (subscribe [:get-contacts])]
    (fn []
      (let [contacts-ds (get-data-source @contacts)]
        [view {:style {:flex            1
                       :backgroundColor color-white}}
         [contact-list-toolbar navigator]
         (when contacts-ds
           [list-view {:dataSource contacts-ds
                       :enableEmptySections true
                       :renderRow  (partial render-row navigator)
                       :style      {:backgroundColor "white"}}])]))))
