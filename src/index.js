export default {
  async fetch(request, env) {
    try {
      const result = await env.DB
        .prepare("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;")
        .all();

      return Response.json(result);
    } catch (err) {
      return new Response(
        JSON.stringify({
          error: String(err),
        }),
        { status: 500 }
      );
    }
  },
};