<script>
    import { Nav, NavItem, NavLink} from 'sveltestrap';
    /** @type {import('./$types').PageData} */
</script>


<Nav Horizontal>
  <NavItem>
    <NavLink href="/standings">Standings</NavLink>
  </NavItem>
  <NavItem>
    <NavLink href="/upcoming">Upcoming</NavLink>
  </NavItem>
</Nav>
<slot></slot>

<svelte:head>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css">
</svelte:head> 



