(ns status-im2.contexts.quo-preview.common
  (:require
    [quo2.core :as quo]
    [quo2.theme :as quo.theme]
    [utils.re-frame :as rf]))


(defn- view-internal
  [{:keys [theme]}]
  (let [logged-in?    (rf/sub [:multiaccount/logged-in?])
        has-profiles? (boolean (rf/sub [:profile/profiles-overview]))
        root          (if has-profiles? :profiles :intro)
        light?        (= theme :light)]
    [quo/page-nav
     {:type       :title
      :title      "Quo2 components preview"
      :text-align :left
      :icon-name  :i/close
      :right-side [{:icon-name (if light? :i/dark :i/light)
                    :on-press  #(if light? (quo.theme/set-theme :dark) (quo.theme/set-theme :light))}]
      :on-press   #(if (or logged-in? (not= (rf/sub [:view-id]) :quo2-preview))
                     (rf/dispatch [:navigate-back])
                     (do
                       (quo.theme/set-theme :dark)
                       (rf/dispatch [:init-root root])))}]))

(def navigation-bar (quo.theme/with-theme view-internal))
