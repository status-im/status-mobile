(ns status-im.contexts.preview.quo.profile.select-profile
  (:require
    [quo.core :as quo]
    [utils.reagent :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [(preview/customization-color-option)
   {:key :name :type :text}
   {:key :selected? :type :boolean}])

(defn view
  []
  (let [state     (reagent/atom {:selected?           false
                                 :name                "Alisher Yakupov"
                                 :customization-color :turquoise
                                 :profile-picture     (resources/get-mock-image :user-picture-male5)})
        selected? (reagent/cursor state [:selected?])]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding 20}}
       [quo/select-profile (merge @state {:on-change #(reset! selected? %)})]])))
