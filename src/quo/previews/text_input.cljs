(ns quo.previews.text-input
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [quo.previews.preview :as preview]
            [quo.react-native :as rn]
            [reagent.core :as reagent]))

(def all-props
  (preview/list-comp [multiline     [false true]
                      label         [nil "Input label"]
                      default-value [nil "Test initial value"]
                      placeholder   [nil "Placeholder value"]
                      before        [nil {:icon :main-icons/search}]
                      after         [nil {:icon :main-icons/close}]
                      error         [nil "Something went wrong!"]
                      secure        [false true]
                      show-cancel   [false true]]
    {:label             label
     :default-value     default-value
     :placeholder       placeholder
     :multiline         multiline
     :before            before
     :after             after
     :error             error
     :show-cancel       show-cancel
     :secure-text-entry secure}))

(def descriptor
  [{:label "Multiline:"
    :key   :multiline
    :type  :boolean}
   {:label "Show cancel:"
    :key   :show-cancel
    :type  :boolean}
   {:label "Secure:"
    :key   :secure-text-entry
    :type  :boolean}
   {:label "After icon:"
    :key   :after
    :type  :boolean}
   {:label "Before icon:"
    :key   :before
    :type  :boolean}
   {:label "Show error:"
    :key   :error
    :type  :boolean}
   {:label "Label"
    :key   :label
    :type  :text}])

(defn render-item
  [props]
  [rn/view
   {:style {:padding-horizontal 16
            :padding-vertical   24}}
   [quo/text-input props]])

(defn cool-preview
  []
  (let [state  (reagent/atom {:secure      false
                              :show-cancel false
                              :multiline   false
                              :label       "I'm a cool label"})
        before (reagent/cursor state [:before])
        after  (reagent/cursor state [:after])
        error  (reagent/cursor state [:error])]
    (fn []
      [rn/view
       {:margin-bottom 50
        :padding       16}
       [preview/customizer state descriptor]
       [quo/text-input
        (merge @state
               {:default-value nil
                :placeholder   "I'm a cool placeholder"
                :before        (when @before {:icon :main-icons/search})
                :after         (when @after {:icon :main-icons/close})
                :error         (when @error "Something went wrong!")})]])))

(defn preview-text
  []
  [rn/view
   {:background-color (:ui-background @colors/theme)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :data                         all-props
     :render-fn                    render-item
     :key-fn                       str}]])
