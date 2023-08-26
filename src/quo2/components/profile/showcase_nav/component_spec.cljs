(ns quo2.components.profile.showcase-nav.component-spec
  (:require [quo2.components.profile.showcase-nav.view :as view]
            [reagent.core :as reagent]
            [test-helpers.component :as h]))

(def nav-data
  [{:icon :i/recent
    :id   :recent}
   {:icon :i/profile
    :id   :profile}
   {:icon :i/communities
    :id   :communities}
   {:icon :i/wallet
    :id   :wallet}
   {:icon :i/nft
    :id   :nft}
   {:icon :i/token
    :id   :token}])

(h/describe "Profile - Showcase nav"
  (h/test "default render"
    (h/render [view/view {:data nav-data}])
    (-> (count (h/query-all-by-label-text :showcase-nav-item))
        (h/expect)
        (.toEqual (count nav-data))))

  (h/test "on press button"
    (let [event (h/mock-fn)]
      (h/render [view/view
                 {:data     nav-data
                  :on-press #(event)}])
      (h/fire-event :press (get (h/get-all-by-label-text :showcase-nav-item) 0))
      (-> (h/expect event)
          (.toHaveBeenCalled))))

  (h/test "active id updated"
    (let [active-id (reagent/atom :recent)]
      (h/render [view/view
                 {:data     nav-data
                  :on-press #(reset! active-id %)}])
      (h/fire-event :press (get (h/get-all-by-label-text :showcase-nav-item) 3))
      (-> (h/expect @active-id)
          (.toStrictEqual :wallet)))))
