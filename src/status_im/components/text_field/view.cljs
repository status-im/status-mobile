(ns status-im.components.text-field.view
  (:require [clojure.string :as s]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [reagent.core :as r]
            [status-im.components.react :refer [react-native
                                                view
                                                text
                                                animated-text
                                                animated-view
                                                text-input
                                                touchable-opacity]]
            [status-im.components.text-field.styles :as st]
            [status-im.i18n :refer [label]]
            [status-im.components.animation :as anim]
            [taoensso.timbre :as log]))


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
                    :line-color        "#0000001f"
                    :focus-line-color  "#0000001f"
                    :error-color       "#d50000"
                    :secure-text-entry false
                    :on-focus          #()
                    :on-blur           #()
                    :on-change-text    #()
                    :on-change         #()})

(defn field-animation [{:keys [top to-top font-size to-font-size
                               line-width to-line-width]}]
  (let [duration (:label-animation-duration config)
        animation (anim/parallel [(anim/timing top {:toValue  to-top
                                                    :duration duration})
                                  (anim/timing font-size {:toValue  to-font-size
                                                          :duration duration})
                                  (anim/timing line-width {:toValue  to-line-width
                                                           :duration duration})])]
    (anim/start animation (fn [arg]
                            (when (.-finished arg)
                              (log/debug "Field animation finished"))))))

; Invoked once before the component is mounted. The return value will be used
; as the initial value of this.state.
(defn get-initial-state [component]
  {:has-focus       false
   :float-label?    false
   :label-top       0
   :label-font-size 0
   :line-width      (anim/create-value 0)
   :max-line-width  100})

; Invoked once, both on the client and server, immediately before the initial
; rendering occurs. If you call setState within this method, render() will see
; the updated state and will be executed only once despite the state change.
(defn component-will-mount [component]
  (let [{:keys [value] :as props} (r/props component)
        data {:label-top       (anim/create-value (if (s/blank? value)
                                                    (:label-bottom config)
                                                    (:label-top config)))
              :label-font-size (anim/create-value (if (s/blank? value)
                                                    (:label-font-large config)
                                                    (:label-font-small config)))
              :float-label?    (if (s/blank? value) false true)}]
    ;(log/debug "component-will-mount")
    (r/set-state component data)))

; Invoked once, only on the client (not on the server), immediately after the
; initial rendering occurs. At this point in the lifecycle, you can access any
; refs to your children (e.g., to access the underlying DOM representation).
; The componentDidMount() method of child components is invoked before that of
; parent components.
(defn component-did-mount [component]
  (let [props (r/props component)]
    ;(log/debug "component-did-mount:")
    ))

; Invoked when a component is receiving new props. This method is not called for
; the initial render. Use this as an opportunity to react to a prop transition
; before render() is called by updating the state using this.setState().
; The old props can be accessed via this.props. Calling this.setState() within
; this function will not trigger an additional render.
(defn component-will-receive-props [component new-props]
  ;(log/debug "component-will-receive-props: new-props=" new-props)
  )

; Invoked before rendering when new props or state are being received. This method
; is not called for the initial render or when forceUpdate is used. Use this as
; an opportunity to return false when you're certain that the transition to the
; new props and state will not require a component update.
; If shouldComponentUpdate returns false, then render() will be completely skipped
; until the next state change. In addition, componentWillUpdate and
; componentDidUpdate will not be called.
(defn should-component-update [component next-props next-state]
  ;(log/debug "should-component-update: " next-props next-state)
  true)

; Invoked immediately before rendering when new props or state are being received.
; This method is not called for the initial render. Use this as an opportunity
; to perform preparation before an update occurs.
(defn component-will-update [component next-props next-state]
  ;(log/debug "component-will-update: " next-props next-state)
  )

; Invoked immediately after the component's updates are flushed to the DOM.
; This method is not called for the initial render. Use this as an opportunity
; to operate on the DOM when the component has been updated.
(defn component-did-update [component prev-props prev-state]
  ;(log/debug "component-did-update: " prev-props prev-state)
  )

(defn on-input-focus [{:keys [component animation onFocus]}]
  (do
    (log/debug "input focused")
    (r/set-state component {:has-focus    true
                            :float-label? true})
    (field-animation animation)
    (when onFocus (onFocus))))

(defn on-input-blur [{:keys [component value animation onBlur]}]
  (log/debug "Input blurred")
  (r/set-state component {:has-focus    false
                          :float-label? (if (s/blank? value) false true)})
  (when (s/blank? value)
    (field-animation animation))
  (when onBlur (onBlur)))

(defn get-width [event]
  (.-width (.-layout (.-nativeEvent event))))

(defn reagent-render [data children]
  (let [component        (r/current-component)
        {:keys [has-focus
                float-label?
                label-top
                label-font-size
                line-width
                current-value
                max-line-width] :as state} (r/state component)
        {:keys [wrapper-style input-style label-hidden? line-color focus-line-color secure-text-entry
                label-color error-color error label value on-focus on-blur
                on-change-text on-change editable] :as props} (merge default-props (r/props component))
        line-color       (if error error-color line-color)
        focus-line-color (if error error-color focus-line-color)
        label-color      (if (and error (not float-label?)) error-color label-color)
        label            (when-not label-hidden?
                           (if error (str label " *") label))]
    [view (merge st/text-field-container wrapper-style)
     (when-not label-hidden?
       [animated-text {:style (st/label label-top label-font-size label-color)} label])
     [text-input {:style             (merge st/text-input input-style)
                  :placeholder       ""
                  :editable          editable
                  :secure-text-entry secure-text-entry
                  :on-focus          #(on-input-focus {:component component
                                                       :animation {:top           label-top
                                                                   :to-top        (:label-top config)
                                                                   :font-size     label-font-size
                                                                   :to-font-size  (:label-font-small config)
                                                                   :line-width    line-width
                                                                   :to-line-width max-line-width}
                                                       :onFocus   on-focus})
                  :on-blur           #(on-input-blur {:component component
                                                      :value     (or current-value value)
                                                      :animation {:top           label-top
                                                                  :to-top        (:label-bottom config)
                                                                  :font-size     label-font-size
                                                                  :to-font-size  (:label-font-large config)
                                                                  :line-width    line-width
                                                                  :to-line-width 0}
                                                      :onBlur    on-blur})
                  :on-change-text    (fn [text]
                                       (r/set-state component {:current-value text})
                                       (on-change-text text))
                  :on-change         #(on-change %)
                  :default-value     value}]
     [view {:style    (st/underline-container line-color)
            :onLayout #(r/set-state component {:max-line-width (get-width %)})}
      [animated-view {:style (st/underline focus-line-color line-width)}]]
     [text {:style (st/error-text error-color)} error]]))

(defn text-field [data children]
  (let [component-data {:get-initial-state            get-initial-state
                        :component-will-mount         component-will-mount
                        :component-did-mount          component-did-mount
                        :component-will-receive-props component-will-receive-props
                        :should-component-update      should-component-update
                        :component-will-update        component-will-update
                        :component-did-update         component-did-update
                        :display-name                 "text-field"
                        :reagent-render               reagent-render}]
    ;(log/debug "Creating text-field component: " data)
    (r/create-class component-data)))
