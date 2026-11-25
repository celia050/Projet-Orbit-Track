import { useEffect, useState } from 'react'

type Article = {
  id: number
  title: string
  content: string
  articleDate: string
  status: string
}

function ArticleDuJour() {
  const [article, setArticle] = useState<Article | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)

  useEffect(() => {
    fetch('/api/articles/today')
      .then((response) => {
        if (response.status === 404) {
          return null
        }

        if (!response.ok) {
          throw new Error('Erreur lors du chargement')
        }

        return response.json() as Promise<Article>
      })
      .then((data) => setArticle(data))
      .catch(() => setError(true))
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return <p>Chargement de l'article du jour...</p>
  }

  if (error) {
    return <p>Impossible de charger l'article du jour.</p>
  }

  if (!article) {
    return <p>Aucun article publié aujourd'hui.</p>
  }

  return (
    <section className="article-du-jour">
      <h2>Article du jour</h2>
      <h3>{article.title}</h3>
      <p>{article.content}</p>
      <small>Date : {article.articleDate}</small>
    </section>
  )
}

export default ArticleDuJour
