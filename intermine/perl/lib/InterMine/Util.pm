package InterMine::Util;

=head1 NAME

InterMine::Util - Utility functions for InterMine

=cut

use strict;

use Exporter;

our @ISA = qw(Exporter);
our @EXPORT_OK = qw(get_property_value get_latest_properties_version $INTERMINE_CONF_DIR);

# location of the InterMine properties files
our $INTERMINE_CONF_DIR = "$ENV{HOME}/.intermine";

=head2 get_property_value
 Title   : get_property_value
 Usage   : $property_value = 
              InterMine::Util::get_property_value('db.production.datasource.serverName',
                                                  '/home/user/flymine.properties');
 Function: gets a value from a properties file
 Args    : $property_name, $property_file_name
=cut

sub get_property_value
{
  my $key = shift;
  my $file = shift;

  open F, "$file" or die "cannot open $file: $!\n";

  my $ret_val;

  while (my $line = <F>) {
    if ($line =~ /^\s*#/) {
      next;
    }
    if ($line =~ /$key=(.*)/) {
      $ret_val = $1;
    }
  }

  close F;

  return $ret_val;
}

=head2 get_latest_properties_version
 Title   : get_latest_properties_version
 Usage   : $version = 
              get_latest_properties_version('flymine.properties.r');
 Function: return the version of the most properties file by looking for files
           starting with the prefix, eg. if these files exist:
           'flymine.properties.r1', 'flymine.properties.r2',
           'flymine.properties.r3' then return "3" as an integer
 Args    : $property_file_name_prefix
=cut
sub get_latest_properties_version
{
  my $prop_file_prefix = shift;

  my $current_version = -1;

  opendir DIR, $INTERMINE_CONF_DIR
    or die "can't open directory $INTERMINE_CONF_DIR for reading\n";

  while (defined(my $filename = readdir(DIR))) {
    if ($filename =~ /^$prop_file_prefix(\d+)$/) {
      if ($1 > $current_version) {
        $current_version = $1;
      }
    }
  }

  closedir DIR;

  if ($current_version == -1) {
    die <<"MESSAGE";
$0: can't find a properties file with prefix:
  "$prop_file_prefix"
in
  $INTERMINE_CONF_DIR
looked for ${prop_file_prefix}0, ${prop_file_prefix}1, ${prop_file_prefix}2, etc
MESSAGE
  }

  return $current_version;
}

1;
