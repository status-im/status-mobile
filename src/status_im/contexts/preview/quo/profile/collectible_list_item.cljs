(ns status-im.contexts.preview.quo.profile.collectible-list-item
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview.quo.preview :as preview]))

(defonce test-image (resources/get-mock-image :collectible))
(defonce test-avatar (resources/get-mock-image :monkey))

(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key :card}
              {:key :image}]}
   {:key  :community?
    :type :boolean}
   {:key     :status
    :type    :select
    :options [{:key :loading}
              {:key :default}
              {:key :unsupported}
              {:key :cant-fetch}]}
   {:key     :gradient-color-index
    :type    :select
    :options [{:key :1}
              {:key :2}
              {:key :3}
              {:key :4}
              {:key :5}
             ]}
   {:key  :counter
    :type :text}
   {:key  :collectible-name
    :type :text}])

(defn view
  []
  (let [state (reagent/atom {:type                 :card
                             :collectible-name     "Doodle #6822"
                             :gradient-color-index :1
                             :community?           false
                             :status               :default
                             :counter              ""})
        width (reagent/atom 0)]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-vertical  20
                                    :margin-horizontal 95}}
       
       
       [rn/view {:on-layout #(reset! width (-> ^js % .-nativeEvent .-layout .-width))}
        [quo/collectible-list-item
         (assoc @state
                :width                @width
                :counter              (when (seq (:counter @state)) (:counter @state))
                :gradient-color-index (js/parseInt (name (:gradient-color-index @state)))
                :image-src            test-image
                :avatar-image-src     test-avatar
                :on-press             #(js/alert "Pressed"))]]])))
