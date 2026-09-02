import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

Deno.serve(async (req) => {
  try {
    const RAPIDAPI_KEY = Deno.env.get('RAPIDAPI_KEY')
    const SUPABASE_URL = Deno.env.get('SUPABASE_URL')
    const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')

    if (!RAPIDAPI_KEY) {
      return new Response(
        JSON.stringify({ success: false, error: 'RAPIDAPI_KEY manquante' }),
        { status: 500, headers: { 'Content-Type': 'application/json' } }
      )
    }

    const supabase = createClient(SUPABASE_URL!, SUPABASE_SERVICE_ROLE_KEY!)

    const today = new Date().toISOString().split('T')[0]

    const apiResponse = await fetch(
      `https://api-football-v1.p.rapidapi.com/v3/fixtures?date=${today}`,
      {
        headers: {
          'x-rapidapi-key': RAPIDAPI_KEY,
          'x-rapidapi-host': 'api-football-v1.p.rapidapi.com',
        },
      }
    )

    if (!apiResponse.ok) {
      return new Response(
        JSON.stringify({ success: false, error: 'Erreur API-Football: ' + apiResponse.status }),
        { status: 502, headers: { 'Content-Type': 'application/json' } }
      )
    }

    const data = await apiResponse.json()
    const fixtures = data.response || []

    let count = 0

    for (const fixture of fixtures) {
      const matchData = {
        api_fixture_id: fixture.fixture?.id ?? null,
        equipe_domicile: fixture.teams?.home?.name ?? null,
        equipe_exterieur: fixture.teams?.away?.name ?? null,
        ligue: fixture.league?.name ?? null,
        date_match: fixture.fixture?.date ?? null,
        statut: fixture.fixture?.status?.short ?? null,
        score_domicile: fixture.goals?.home ?? null,
        score_exterieur: fixture.goals?.away ?? null,
      }

      if (matchData.api_fixture_id === null) continue

      const { error } = await supabase
        .from('match')
        .upsert(matchData, { onConflict: 'api_fixture_id' })

      if (!error) count++
    }

    return new Response(
      JSON.stringify({ success: true, matchs_synchronises: count }),
      { status: 200, headers: { 'Content-Type': 'application/json' } }
    )
  } catch (err) {
    return new Response(
      JSON.stringify({ success: false, error: String(err) }),
      { status: 500, headers: { 'Content-Type': 'application/json' } }
    )
  }
})
