(ns status-im.ui.components.text-field.view
  (:require [clojure.string :as string]
            [reagent.core :as reagent]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.text-field.styles :as styles]
            [status-im.ui.components.animation :as animation]
            [taoensso.timbre :as log]
            [status-im.ui.components.styles :as components.styles]))


(def config {:label-top                16
             :label-bottom             37
             :label-font-large         16
             :label-font-small         13
             :label-animation-duration 200})

(def default-props {:wrapper-style     {}
                    :input-style       {}
                    :line-style        {}
                    :editable          true
                    :label-color       "#838c93"
                    :line-color        components.styles/separator-color
                    :focus-line-color  components.styles/separator-color
                    :focus-line-height 1
                    :error-color       "#d50000"
                    :secure-text-entry false
                    :on-focus          #()
                    :on-blur           #()
                    :on-change-text    #()
                    :on-change         #()
                    :auto-capitalize   :sentences})

(defn field-animation [{:keys [top to-top font-size to-font-size
                               line-width to-line-width line-height to-line-height]} & [value-blank?]]
  (let [duration  (:label-animation-duration config)
        animation (animation/parallel (into []
                                            (concat
                                     (when (or (nil? value-blank?) value-blank?)
                                       [(animation/timing top {:toValue to-top
                                                          :duration     duration})
                                        (animation/timing font-size {:toValue to-font-size
                                                                :duration     duration})])
                                     [(animation/timing line-width {:toValue to-line-width
                                                               :duration     duration})
                                      (animation/timing line-height {:toValue to-line-height
                                                                :duration     duration})])))]
    (animation/start animation (fn [arg]
                            (when (.-finished arg)
                              (log/debug "Field animation finished"))))))

; Invoked once before the component is mounted. The return value will be used
; as the initial value of this.state.
(defn get-initial-state [_]
  {:has-focus       false
   :float-label?    false
   :label-top       0
   :label-font-size 0
   :line-width      (animation/create-value 0)
   :line-height     (animation/create-value 1)
   :max-line-width  100})

; Invoked once, both on the client and server, immediately before the initial
; rendering occurs. If you call setState within this method, render() will see
; the updated state and will be executed only once despite the state change.
(defn component-will-mount [component]
  (let [{:keys [value]} (reagent/props component)
        data {:label-top       (animation/create-value (if (string/blank? value)
                                                    (:label-bottom config)
                                                    (:label-top config)))
              :label-font-size (animation/create-value (if (string/blank? value)
                                                    (:label-font-large config)
                                                    (:label-font-small config)))
              :float-label?    (if (string/blank? value) false true)}]
    ;(log/debug "component-will-mount")
    (reagent/set-state component data)))

(defn on-input-focus [{:keys [component animation onFocus]}]
  (do
    (log/debug "input focused")
    (reagent/set-state component {:has-focus true
                            :float-label?    true})
    (field-animation (merge animation
                            {:to-line-width (:max-line-width (reagent/state component))}))
    (when onFocus (onFocus))))

(defn on-input-blur [{:keys [component value animation onBlur]}]
  (log/debug "Input blurred")
  (reagent/set-state component {:has-focus false
                          :float-label?    (if (string/blank? value) false true)})
  (field-animation animation (string/blank? value))
  (when onBlur (onBlur)))

(defn get-width [event]
  (.-width (.-layout (.-nativeEvent event))))

(defn reagent-render [_ _]
  (let [component        (reagent/current-component)
        input-ref        (reagent/atom nil)
        {:keys [float-label?
                label-top
                label-font-size
                line-width
                line-height
                current-value
                valid-value
                temp-value
                max-length]} (reagent/state component)
        {:keys [wrapper-style input-style label-hidden? line-color focus-line-color focus-line-height
                secure-text-entry label-color error-color error label value on-focus on-blur validator
                auto-focus on-change-text on-change on-end-editing editable placeholder
                placeholder-text-color auto-capitalize multiline number-of-lines]}
        (merge default-props (reagent/props component))
        valid-value      (or valid-value "")
        line-color       (if error error-color line-color)
        focus-line-color (if error error-color focus-line-color)
        label-color      (if (and error (not float-label?)) error-color label-color)
        label            (when-not label-hidden?
                           (if error (str label " *") label))]
    [react/view (merge styles/text-field-container wrapper-style)
     (when-not label-hidden?
       [react/animated-text {:style (styles/label label-top label-font-size label-color)} label])
     [react/text-input {:ref                    #(reset! input-ref %)
                  :style                  (merge styles/text-input input-style)
                  :placeholder            (or placeholder "")
                  :placeholder-text-color placeholder-text-color
                  :editable               editable
                  :multiline              multiline
                  :number-of-lines        number-of-lines
                  :secure-text-entry      secure-text-entry
                  :auto-capitalize        auto-capitalize
                  :on-focus               #(on-input-focus {:component component
                                                            :animation {:top            label-top
                                                                        :to-top         (:label-top config)
                                                                        :font-size      label-font-size
                                                                        :to-font-size   (:label-font-small config)
                                                                        :line-width     line-width
                                                                        :line-height    line-height
                                                                        :to-line-height focus-line-height}
                                                            :onFocus   on-focus})
                  :on-blur                #(on-input-blur {:component component
                                                           :value     (or current-value value)
                                                           :animation {:top            label-top
                                                                       :to-top         (:label-bottom config)
                                                                       :font-size      label-font-size
                                                                       :to-font-size   (:label-font-large config)
                                                                       :line-width     line-width
                                                                       :line-height    line-height
                                                                       :to-line-width  0
                                                                       :to-line-height 1}
                                                           :onBlur    on-blur})
                  :on-change-text         (fn [text]
                                            (reagent/set-state component {:current-value text})
                                            (if (or (not validator) (validator text))
                                              (do
                                                (reagent/set-state component {:valid-value text
                                                                        :temp-value        nil})
                                                (on-change-text text))
                                              (reagent/set-state component {:temp-value valid-value
                                                                      :max-length       (count valid-value)})))
                  :on-change              on-change
                  :default-value          value
                  :value                  temp-value
                  :max-length             max-length
                  :on-submit-editing      #(.blur @input-ref)
                  :on-end-editing         (when on-end-editing on-end-editing)
                  :auto-focus             (true? auto-focus)}]
     [react/view {:style    (styles/underline-container line-color)
            :onLayout #(reagent/set-state component {:max-line-width (get-width %)})}
      [react/animated-view {:style (styles/underline focus-line-color line-width line-height)}]]
     [react/text {:style (styles/error-text error-color)} error]]))

(defn text-field [_ _]
  (let [component-data {:get-initial-state    get-initial-state
                        :component-will-mount component-will-mount
                        :display-name         "text-field"
                        :reagent-render       reagent-render
                        :component-did-update (fn [comp]
                                                (let [{:keys [temp-value]} (reagent/state comp)]
                                                  (when temp-value
                                                    (reagent/set-state comp {:temp-value nil
                                                                       :max-length       nil}))))}]
    ;(log/debug "Creating text-field component: " data)
    (reagent/create-class component-data)))
