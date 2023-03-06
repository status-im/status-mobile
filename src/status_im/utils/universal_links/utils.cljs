(ns status-im.utils.universal-links.utils
  (:require [goog.string :as gstring]
            [status-im2.constants :as constants]))

;; domains should be without the trailing slash
(def domains
  {:external "https://join.status.im"
   :internal "status-im:/"})

(def links
  {:public-chat  "%s/%s"
   :private-chat "%s/p/%s"
   :user         "%s/u/%s"
   :browse       "%s/b/%s"})

(defn universal-link?
  [url]
  (boolean
   (re-matches constants/regx-universal-link url)))

(defn deep-link?
  [url]
  (boolean
   (re-matches constants/regx-deep-link url)))

(defn generate-link
  [link-type domain-type param]
  (gstring/format (get links link-type)
                  (get domains domain-type)
                  param))
