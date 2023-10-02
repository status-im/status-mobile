(ns status-im2.contexts.quo-preview.settings.settings-item
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [status-im2.common.resources :as resources]
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key  :title
    :type :text}
   {:key     :action
    :type    :select
    :options [{:key   nil
               :value :none}
              {:key   :arrow
               :value :arrow}
              {:key   :button
               :value :button}
              {:key   :selector
               :value :selector}]}
   {:key     :label
    :type    :select
    :options [{:key   nil
               :value :none}
              {:key   :color
               :value :color}
              {:key   :preview
               :value :preview}
              {:key   :text
               :value :text}]}
   {:key     :image
    :type    :select
    :options [{:key   nil
               :value :none}
              {:key   :icon
               :value :icon}
              {:key   :avatar
               :value :avatar}
              {:key   :icon-avatar
               :value :icon-avatar}]}
   {:key     :description
    :type    :select
    :options [{:key   nil
               :value :none}
              {:key   :text
               :value :text}
              {:key   :text-plus-icon
               :value :text-plus-icon}
              {:key   :status
               :value :status}]}
   {:key     :tag
    :type    :select
    :options [{:key   nil
               :value :none}
              {:key   :positive
               :value :positive}
              {:key   :context
               :value :context}]}
   {:key  :blur?
    :type :boolean}])

(def communities-list
  [{:source (resources/get-mock-image :coinbase)}
   {:source (resources/get-mock-image :decentraland)}
   {:source (resources/get-mock-image :rarible)}])

(defn get-props
  [data]
  (when (:toggle-props data) (js/console.warn data))
  (merge
   data
   {:image-props       (case (:image data)
                         :icon        :i/browser
                         :avatar      {:full-name           "A Y"
                                       :size                :xxs
                                       :customization-color :blue}
                         :icon-avatar {:size  :medium
                                       :icon  :i/placeholder
                                       :color :blue}
                         nil)
    :description-props (case (:description data)
                         :text           {:text "This is a description"}
                         :text-plus-icon {:text "This is a description"
                                          :icon :i/placeholder}
                         :status         {:online? true}
                         nil)
    :action-props      (case (:action data)
                         :button {:on-press    #(js/alert "Button pressed!")
                                  :button-text "Button"}
                         nil)
    :label-props       (case (:label data)
                         :text    "Label"
                         :color   :blue
                         :preview {:type :communities
                                   :data communities-list}
                         nil)
    :tag-props         (case (:tag data)
                         :context {:icon    :i/placeholder
                                   :context "Context"}
                         nil)}))

(defn preview
  []
  (let [state (reagent/atom {:title               "Account"
                             :accessibility-label :settings-item
                             :action              :arrow
                             :image               :icon
                             :blur?               false
                             :on-press            (fn [] (js/alert "Settings list item pressed"))})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true
        :blur-dark-only?       true}
       [rn/view {:style {:flex 1}}
        [quo/settings-item (get-props @state)]]])))
