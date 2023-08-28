(ns quo2.components.wallet.progress-bar.component-spec
  (:require [test-helpers.component :as h]
            [quo2.components.wallet.progress-bar.view :as progress-bar]
            [quo2.foundations.colors :as colors]))

(h/describe "Progress bar"
  (h/test "pending state in light mode"
    (let [theme :light
          props {:state               :pending
                 :customization-color :blue}]
      (h/render-with-theme-provider [progress-bar/view props] theme)
      (h/has-style (h/query-by-label-text :progress-bar)
                   {:height          12
                    :width           8
                    :borderRadius    3
                    :borderColor     colors/neutral-80-opa-5
                    :backgroundColor colors/neutral-5})))

  (h/test "pending state in dark mode"
    (let [theme :dark
          props {:state               :pending
                 :customization-color :blue}]
      (h/render-with-theme-provider [progress-bar/view props] theme)
      (h/has-style (h/query-by-label-text :progress-bar)
                   {:height          12
                    :width           8
                    :borderRadius    3
                    :borderColor     colors/neutral-70
                    :backgroundColor colors/neutral-80})))

  (h/test "finalized state with customtization-color blue in light mode"
    (let [theme :light
          props {:state               :finalized
                 :customization-color :blue}]
      (h/render-with-theme-provider [progress-bar/view props] theme)
      (h/has-style (h/query-by-label-text :progress-bar)
                   {:height          12
                    :width           8
                    :borderRadius    3
                    :borderColor     colors/neutral-80-opa-5
                    :backgroundColor (colors/custom-color (:customization-color props) 50)})))

  (h/test "finalized state with customtization-color blue in dark mode"
    (let [theme :dark
          props {:state               :finalized
                 :customization-color :blue}]
      (h/render-with-theme-provider [progress-bar/view props] theme)
      (h/has-style (h/query-by-label-text :progress-bar)
                   {:height          12
                    :width           8
                    :borderRadius    3
                    :borderColor     colors/neutral-80-opa-5
                    :backgroundColor (colors/custom-color (:customization-color props) 60)})))

  (h/test "finalized state with customtization-color army in light mode"
    (let [theme :light
          props {:state               :finalized
                 :customization-color :army}]
      (h/render-with-theme-provider [progress-bar/view props] theme)
      (h/has-style (h/query-by-label-text :progress-bar)
                   {:height          12
                    :width           8
                    :borderRadius    3
                    :borderColor     colors/neutral-80-opa-5
                    :backgroundColor (colors/custom-color (:customization-color props) 50)})))

  (h/test "confirmed state in light mode"
    (let [theme :light
          props {:state               :confirmed
                 :customization-color :blue}]
      (h/render-with-theme-provider [progress-bar/view props] theme)
      (h/has-style (h/query-by-label-text :progress-bar)
                   {:height          12
                    :width           8
                    :borderRadius    3
                    :borderColor     colors/neutral-80-opa-5
                    :backgroundColor colors/success-50})))

  (h/test "confirmed state in dark mode"
    (let [theme :dark
          props {:state               :confirmed
                 :customization-color :blue}]
      (h/render-with-theme-provider [progress-bar/view props] theme)
      (h/has-style (h/query-by-label-text :progress-bar)
                   {:height          12
                    :width           8
                    :borderRadius    3
                    :borderColor     colors/white-opa-5
                    :backgroundColor colors/success-60})))

  (h/test "error state in light mode"
    (let [theme :light
          props {:state               :error
                 :customization-color :blue}]
      (h/render-with-theme-provider [progress-bar/view props] theme)
      (h/has-style (h/query-by-label-text :progress-bar)
                   {:height          12
                    :width           8
                    :borderRadius    3
                    :borderColor     colors/neutral-80-opa-5
                    :backgroundColor colors/danger-50})))

  (h/test "error state in dark mode"
    (let [theme :dark
          props {:state               :error
                 :customization-color :blue}]
      (h/render-with-theme-provider [progress-bar/view props] theme)
      (h/has-style (h/query-by-label-text :progress-bar)
                   {:height          12
                    :width           8
                    :borderRadius    3
                    :borderColor     colors/white-opa-5
                    :backgroundColor colors/danger-60}))))
