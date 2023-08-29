(ns status-im2.contexts.quo-preview.links.url-preview
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.common.resources :as resources]
    [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Title"
    :key   :title
    :type  :text}
   {:label "Body"
    :key   :body
    :type  :text}
   {:label "With logo?"
    :key   :with-logo?
    :type  :boolean}
   {:label "Loading?"
    :key   :loading?
    :type  :boolean}
   {:label "Loading message"
    :key   :loading-message
    :type  :text}])

(defn cool-preview
  []
  (let [state (reagent/atom
               {:title           "Status - Private, Secure Communication"
                :body            "Status.im"
                :with-logo?      true
                :loading?        false
                :loading-message "Generating preview"})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:style {:padding-bottom 150}}
        [preview/customizer state descriptor]
        [rn/view
         {:style {:align-items        :center
                  :padding-horizontal 16
                  :margin-top         50}}
         [quo/url-preview
          {:title           (:title @state)
           :body            (:body @state)
           :logo            (when (:with-logo? @state)
                              (resources/get-mock-image :status-logo))
           :loading?        (:loading? @state)
           :loading-message (:loading-message @state)
           :on-clear        #(js/alert "Clear button pressed")}]]]])))

(defn preview
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/white colors/neutral-95)
            :flex             1}}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
